# Routes

Proof of concept Android transit application that I used to try out some new Android concepts that I've never used before:

- Jetpack Compose
- Hilt
- Paging
- Kotlin coroutines
- SQLite Full-Text Search (FTS)

## How do you run the app?

Before you can run the app, you'll need to run the script to generate the SQLite database that contains all the information such as bus stops, routes, times, etc.

### How do you generate the database?

1. Make sure to download Node.js (14+), and [yarn classic](https://classic.yarnpkg.com/en/docs/install) (npm can be used but yarn is recommended since there's a yarn.lock file included).
1. To retrieve the files to generate the local database, download the zip file from here (https://www.octranspo.com/files/google_transit.zip) and extract the files to `scripts/gtfs-converter/ottawa`.
1. Open your terminal and cd into `scripts/gtfs-converter`.
1. Install necessary dependencies by running `yarn install`.
1. Compile the Typescript to Javascript by running `yarn build` in your terminal.
1. Generate the database by running `yarn start`.
1. Once the database is generated, copy the new .sqlite file into `app/src/main/assets/databases`

Now that you've generated the database, you can now just open the project in Android Studio and click Run!
