# GenerateSyntheticDatasets
A tool to generate a new dataset containing redundant attributes (by mathematical expressions) from other dataset. It can be used to evaluate [dimensionality reduction](https://en.wikipedia.org/wiki/Dimensionality_reduction) algorithms and the influence of curse of high-dimensionality on their results.

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE.md)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/832b77c7785745ebb60ca538cabc0640)](https://www.codacy.com/app/jadsonjjmo/GenerateSyntheticDatasets?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jadsonjjmo/GenerateSyntheticDatasets&amp;utm_campaign=Badge_Grade)
[![Build Status](https://travis-ci.com/jadsonjjmo/GenerateSyntheticDatasets.svg?branch=master)](https://travis-ci.com/jadsonjjmo/GenerateSyntheticDatasets)
[![codecov](https://codecov.io/gh/jadsonjjmo/GenerateSyntheticDatasets/branch/master/graph/badge.svg)](https://codecov.io/gh/jadsonjjmo/GenerateSyntheticDatasets)

## How to use

This tool makes use of [Apache Maven](https://maven.apache.org) for practicality and application automation purposes.
To compile this package, simply install Apache Maven and run the command: `mvn clean compile assembly:single`.
Otherwise you'll need to manually setup the dependency [JSON In Java](https://mvnrepository.com/artifact/org.json/json).

To generate new redundant attributes, you must enter the equivalent expression in a configuration file in the [Json](https://pt.wikipedia.org/wiki/JSON) format, 
as well as the path of the original dataset, the path of the new dataset (with redundant attributes), and the char separator (for example: "," for .csv files).


Here's a basic example of the configuration file formatting:

```Json
{
  "origin_dataset": "dataset.csv",
  "target_dataset": "new_dataset.csv",
  "separator": " ",
  "redundant_attributes":[
     "{2}*[0]+{1.34}",
     "{2}*[0]+{1.34}",
     "{2}*([0]+[1])+{1.34}"
  ]
}
```

After entering all the required settings, run the following command:

`java -jar target/GenerateSyntheticDataset-1.0-SNAPSHOT-jar-with-dependencies.jar <path-of-config-file>.json`

Or run the [script.sh](/script.sh) file.

```shell
./script.sh
```

### How to test

This tool is composed for a set for unit tests, created with the [JUnit](https://pt.wikipedia.org/wiki/JUnit) library.

To run the existing unit tests, with the Apache Maven installed correctly, just run the following command:

`mvn test`
