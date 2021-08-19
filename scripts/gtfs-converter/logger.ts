import dayjs from "dayjs";
import { createWriteStream } from "fs";
import pino from "pino";
import pinoMulti from "pino-multi-stream";

const prettyStream = pinoMulti.prettyStream({
  prettyPrint: {
    translateTime: true,
  },
});

export const logger = pino(
  {
    level: "trace",
  },
  pinoMulti.multistream([
    { stream: prettyStream, level: "info" },
    {
      stream: createWriteStream(
        // TODO: change logger to pass city name
        `./ottawa-${dayjs().format("YYYY-MM-DD_HH-mm-ss")}.log`
      ),
      level: "trace",
    },
  ])
);
