import parse from "csv-parse";
import { createReadStream, readdir, ReadStream, unlink } from "fs";
import { Database } from "sqlite";
import { promisify } from "util";

import { config, IndexKey, TableName } from "./config";
import { init as initDb } from "./db";
import { logger } from "./logger";
import { promisesConcat } from "./promise";

const deleteFile = promisify(unlink);
const getFiles = promisify(readdir);

const folderPath = "./ottawa";
const savedColumns: Record<string, string[]> = {};

function getDb(folderPath: string) {
  return initDb(`${folderPath}.sqlite`);
}

function getColumnsWithTypes(columns: string[][]) {
  return columns.map(([column, value]) => {
    let dataType = "TEXT";
    const num = Number(value);

    if (!isNaN(num)) {
      dataType = num % 1 === 0 ? "INTEGER" : "REAL";
    } else if (
      value.toLowerCase() === "true" ||
      value.toLowerCase() === "false"
    ) {
      dataType = "INTEGER";
    }

    return `${column} ${dataType}`;
  });
}

function getTableName(fileName: string): TableName {
  return fileName.split("/").pop()!.replace(".txt", "") as TableName;
}

function createTable(
  db: Database,
  tableName: string,
  columns: string[],
  keys: Partial<Record<IndexKey, string[]>>,
  isVirtual = false
) {
  let sql: string;

  if (isVirtual) {
    sql = `CREATE VIRTUAL TABLE ${tableName}_vrt USING fts4(${columns.join(
      ", "
    )});`;
  } else {
    sql = `CREATE TABLE IF NOT EXISTS ${tableName} (${columns.join(", ")}`;

    const { primaryKey, foreignKeys, unique: uniqueKeys } = keys;
    if (primaryKey) {
      sql += `, PRIMARY KEY (${primaryKey.join(", ")})`;
    }
    if (uniqueKeys) {
      sql += `, UNIQUE (${uniqueKeys.join(", ")})`;
    }
    if (foreignKeys) {
      foreignKeys.forEach((foreignKey) => {
        const [otherTableName, column] = foreignKey.split(".");
        sql += `, FOREIGN KEY (${column}) REFERENCES ${otherTableName} (${column})`;
      });
    }

    sql += ");";
  }

  return db.run(sql);
}

async function insertRows(
  db: Database,
  tableName: string,
  sql: string,
  rows: any[][],
  parameters: string
) {
  logger.info("%s %d", tableName, rows.length);

  await db.run(sql + rows.map(() => parameters).join(", ") + ";", rows.flat());
}

async function parseAndInsertRows(
  fileStream: ReadStream,
  db: Database,
  tableName: TableName
) {
  const parser = fileStream.pipe(
    parse({
      columns: true,
      trim: true,
      cast: true,
    })
  );

  let hasCreatedTable = false;
  let rows: any[][] = [];
  let columns: string[] = [];
  let sql = "";
  let parameters = "";

  for await (const record of parser) {
    if (!hasCreatedTable) {
      await createTable(
        db,
        tableName,
        getColumnsWithTypes(Object.entries(record)),
        config.tables[tableName]
      );
      const indexes = config.tables[tableName].index;
      if (indexes) {
        for (let i = 0; i < indexes.length; i++) {
          const index = indexes[i];
          await db.run(
            `CREATE ${
              index.isUnique ? "UNIQUE" : ""
            } INDEX ${tableName}_${i}_idx ON ${tableName} (${index.columns.join(
              ","
            )});`
          );
        }
      }

      columns = Object.keys(record);
      if (
        config.combine.some((tables) =>
          Object.keys(tables).some((table) => table === tableName)
        )
      ) {
        savedColumns[tableName] = getColumnsWithTypes(Object.entries(record));
      }
      sql = `INSERT INTO ${tableName} (${columns.join(", ")}) VALUES `;
      parameters = `(${columns.map(() => "?").join(", ")})`;
      hasCreatedTable = true;
    }

    if ((rows.length + 1) * columns.length > 999) {
      await insertRows(db, tableName, sql, rows, parameters);
      rows.length = 0;
    }
    rows.push(Object.values(record));
  }
  await insertRows(db, tableName, sql, rows, parameters);
}

async function createSearchTable(db: Database, tableName: string) {
  logger.debug(`possibly creating search table for %s`, tableName);
  const searchableOption = config.searchable.find(
    ({ name }) => tableName === name
  );
  if (searchableOption) {
    const columns = searchableOption.columns.map(
      (column) => column.split(".").pop()!
    );
    logger.debug(
      `creating search table for %s with columns %s`,
      tableName,
      columns.join(", ")
    );
    const tables = Array.from(
      new Set([
        tableName,
        ...searchableOption.columns
          .filter((columnName) => columnName.includes("."))
          .map((columnName) => columnName.split(".")[0]),
      ])
    );
    let sql = `INSERT INTO ${tableName}_vrt SELECT DISTINCT ${searchableOption.columns.join(
      ", "
    )} FROM ${tables.join(", ")}`;
    if (searchableOption.where) {
      sql += " WHERE " + searchableOption.where;
    }
    logger.debug(sql);

    try {
      await createTable(db, tableName, columns, {}, true);
      await db.run(sql);
    } catch (error) {
      if (/table .+ already exists/.test(error.message)) {
        return;
      }
      throw error;
    }
  }
}

async function createTableFromCsv(filePath: string) {
  const db = await getDb(folderPath);
  const tableName = getTableName(filePath);

  await parseAndInsertRows(
    createReadStream(`${folderPath}/${filePath}`),
    db,
    tableName
  );
  logger.debug(filePath);
}

async function createSearchableTable(filePath: string) {
  const db = await getDb(folderPath);
  const tableName = getTableName(filePath);
  await createSearchTable(db, tableName);
}

function getTableCreationOrder(filePaths: string[]) {
  let filePathTracking = [...filePaths];
  const order: string[][] = [];

  while (filePathTracking.length > 0) {
    const addToOrder = [];

    for (let i = 0; i < filePathTracking.length; i++) {
      const tableName = getTableName(filePathTracking[i]);
      const foreignKeys = config.tables[tableName].foreignKeys;

      if (
        !foreignKeys ||
        (order.length > 0 &&
          foreignKeys.every((key) =>
            order.some((tablesCreated) =>
              tablesCreated.some(
                (fileName) => getTableName(fileName) === key.split(".")[0]
              )
            )
          ))
      ) {
        addToOrder.push(filePathTracking[i]);
        filePathTracking = filePathTracking
          .slice(0, i)
          .concat(filePathTracking.slice(i + 1));
        i--;
      }
    }

    order.push(addToOrder);
  }

  return order;
}

async function createTables() {
  const filePaths = (await getFiles(folderPath)).filter(
    (filePath) => config.tables[getTableName(filePath)] !== undefined
  );
  const db = await getDb(folderPath);

  for (const tablePathsToCreate of getTableCreationOrder(filePaths)) {
    logger.info("starting transaction");
    await db.run("BEGIN TRANSACTION;");
    await Promise.all(
      tablePathsToCreate.map((filePath) => createTableFromCsv(filePath))
    );
    logger.info("committing transaction");
    await db.run("COMMIT;");
  }
  await promisesConcat(
    filePaths.map((filePath) => () => createSearchableTable(filePath))
  );
}

async function combineTables() {
  const db = await getDb(folderPath);
  const keysToSkip = ["primaryKey", "foreignKeys", "unique", "where", "index"];
  const tablesToDelete = new Set<string>();

  for (const combinedTables of config.combine) {
    const tables: TableName[] = Object.keys(combinedTables).filter(
      (key): key is TableName => !keysToSkip.includes(key)
    );
    logger.debug("combining tables %s", tables.join(", "));
    const tableName = tables.join("_");
    let createTableSql = `CREATE TABLE ${tableName} (${tables
      .map((tableName) => {
        let columns = combinedTables[tableName]?.map(
          (column) =>
            savedColumns[tableName].find(
              (columnWithType) => columnWithType.split(" ")[0] === column
            )!
        );
        if (!columns) {
          columns = savedColumns[tableName];
        }
        return columns;
      })
      .join(", ")}`;

    const {
      primaryKey,
      unique: uniqueKeys,
      foreignKeys,
      index: indexes,
    } = combinedTables;
    if (primaryKey) {
      createTableSql += `, PRIMARY KEY (${primaryKey.join(", ")})`;
    }
    if (uniqueKeys) {
      createTableSql += `, UNIQUE (${uniqueKeys.join(", ")})`;
    }
    if (foreignKeys) {
      foreignKeys.forEach((foreignKey) => {
        const [otherTableName, column] = foreignKey.split(".");
        createTableSql += `, FOREIGN KEY (${column}) REFERENCES ${otherTableName} (${column})`;
      });
    }

    createTableSql += ");";
    logger.debug(createTableSql);
    await db.run(createTableSql);

    if (indexes) {
      for (let i = 0; i < indexes.length; i++) {
        const index = indexes[i];
        await db.run(
          `CREATE ${
            index.isUnique ? "UNIQUE" : ""
          } INDEX ${tableName}_${i}_idx ON ${tableName} (${index.columns.join(
            ","
          )});`
        );
      }
    }

    const insertSql = `INSERT INTO ${tableName} SELECT ${tables
      .map((tableName) => {
        const columns = combinedTables[tableName]?.map(
          (column) => `${tableName}.${column}`
        );
        if (columns) {
          return columns;
        }
        return `${tableName}.*`;
      })
      .flat()
      .join(", ")} FROM ${tables.join(", ")} WHERE ${combinedTables.where[0]};`;
    logger.debug(insertSql);
    await db.run(insertSql);

    for (const table of tables) {
      tablesToDelete.add(table);
    }
  }

  await promisesConcat(
    Array.from(tablesToDelete.values()).map((table) => () => {
      const dropTableSql = `DROP TABLE ${table};`;
      logger.debug(dropTableSql);
      return db.run(dropTableSql);
    })
  );
}

async function main() {
  try {
    await deleteFile("./ottawa.sqlite");
  } catch (error) {
    if (!error.message.includes("no such file")) {
      throw error;
    }
  }
  await createTables();
  await combineTables();
}

main().catch((err) => logger.error(err));
