# SUSTech-CS109-Othello

## Introduction
Project for CS109, SUSTech, 2021 Fall.

This project is an implementation of the Othello game using Froster/2DBoardGameFrame.

由于是大一上写的代码，所以可能有很多特性（bug）。

## Usage
1. Clone the repository
```bash
$ git clone https://github.com/squarezhong/SUSTech-CS109-Othello
```

2. Install the 2DBoardGameFrame

Make sure you have installed the Maven and JDK 17.

You can also use other versions (>= 9) of the JDK. But you need to modify the `pom.xml` file. 

```bash
$ wget https://github.com/Fros1er/2DBoardGameFrame/releases/download/v0.2.3-alpha/2DBoardGameFrame-0.2.3-Alpha.jar
$ mvn install:install-file -Dfile=./2DBoardGameFrame-0.2.3-Alpha.jar -DgroupId=com.froster -DartifactId=2DBoardGameFrame -Dversion=0.2.3 -Dpackaging=jar
```

3. Compile the project
```bash
$ mvn clean
$ mvn compile
```

4. Run the project
```bash
$ mvn exec:java -Dexec.mainClass="com.squarezhong.Othello"
```

## Algorithm
Simple greedy algorithm. (菜的一逼)

## TODO
- [ ] Simple alpha-beta pruning algorithm

## License
Apache License 2.0