import {open} from "sqlite";
import sqlite3 from "sqlite3";

export function init(dbPath = "./db.sqlite") {
  return open({ filename: dbPath, driver: sqlite3.Database });
}
