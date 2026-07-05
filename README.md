# SauceDemo Selenium Framework

Selenium 4 + TestNG automation framework for saucedemo.com. POM design, data-driven from Excel, cross-browser (Chrome/Firefox/Edge), Docker Grid support, ExtentReports, auto-retry on failure.

## Run tests directly in GitHub (no local setup needed)

1. Go to the Actions tab of this repo.
2. Select "SauceDemo Automation Suite" from the left sidebar.
3. Click "Run workflow", pick a suite file, click "Run workflow" again.
4. Wait for the run to finish (green check or red X in the Actions list).
5. Open the latest report at:
   `https://<your-github-username>.github.io/<repo-name>/`

The report updates automatically after every run. No download or clone required.

First-time setup for the Pages link to work: repo Settings -> Pages -> set source to the `gh-pages` branch (this branch is created automatically the first time the workflow runs).

## Structure

```
qa-automation-framework/
├── pom.xml
├── testng.xml
├── testng-chrome-only.xml
├── docker-compose.yml
├── .github/workflows/run-tests.yml
├── src/test/java/com/saucedemo/
│   ├── base/BaseTest.java
│   ├── pages/
│   ├── utils/
│   ├── listeners/
│   └── tests/
└── src/test/resources/
    ├── TestData.xlsx
    └── config.properties
```

## Run locally

Chrome only:
```
mvn clean test -DsuiteXmlFile=testng-chrome-only.xml
```

Full cross-browser (needs Chrome, Firefox, Edge installed):
```
mvn clean test
```

Headless:
```
mvn clean test -Dheadless=true
```

Against the Docker Grid:
```
docker-compose up -d
mvn clean test -Dexecution.mode=remote -Dgrid.url=http://localhost:4444/wd/hub
```

## Config

`src/test/resources/config.properties` — base URL, execution mode, grid URL, headless flag, edge binary path override. All overridable via `-D` flags or env vars.

## Reports

`target/ExtentReport.html` after each run, with screenshots embedded inline on failure.
