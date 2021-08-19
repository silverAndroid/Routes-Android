require("dotenv").config();

export type IndexKey = "primaryKey" | "foreignKeys" | "unique";
export type Combinable = Partial<Record<IndexKey, string[]>> &
  // needs to be string array (because of condition below) but only first index will be read
  Record<"where", string[]> &
  Partial<Record<"index", Index[]>> &
  Partial<Record<TableName, string[] | null>>;

export type TableName = "stops" | 'routes' | 'trips' | 'stop_times' | 'calendar';

interface Index {
  /** columns to index on */
  columns: string[];
  /** whether index should be a unique one or not */
  isUnique: boolean;
}

interface Searchable {
  name: string;
  columns: string[];
  where?: string;
}

export interface Config {
  sqlitePath: string | undefined;
  tables: Record<
    TableName,
    Partial<Record<IndexKey, string[]>> & {
      index?: Index[];
    }
  >;
  searchable: Searchable[];
  combine: Combinable[];
}

export const config: Config = {
  sqlitePath: process.env.SQLITE_PATH,
  tables: {
    stops: {
      primaryKey: ["stop_id"],
      index: [{ columns: ["stop_code"], isUnique: false }],
    },
    routes: {
      unique: ["route_id"],
    },
    trips: {
      unique: ["trip_id", "route_id"],
      foreignKeys: ["routes.route_id"],
    },
    stop_times: {
      primaryKey: ["stop_id", "trip_id", "stop_sequence"],
      foreignKeys: ["stops.stop_id", "trips.trip_id"],
    },
    calendar: {
      primaryKey: ["service_id"],
    },
  },
  searchable: [
    {
      name: "stops",
      columns: ["stop_code", "stop_name"],
    },
    {
      name: "routes",
      columns: ["route_short_name", "trips.trip_headsign"],
      where: "routes.route_id = trips.route_id",
    },
  ],
  combine: [
    {
      trips: [
        "service_id",
        "trip_id",
        "trip_headsign",
        "direction_id",
        "block_id",
      ],
      routes: null,
      where: ["routes.route_id = trips.route_id"],
      primaryKey: ["route_id", "trip_id"],
      foreignKeys: ["calendar.service_id"],
      index: [{ columns: ["trip_id"], isUnique: true }],
    },
  ],
};
