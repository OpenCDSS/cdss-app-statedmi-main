# eclipse-build-issues-notes

Trying to build StateDMI into installer using ***External Tools Configurations / cdss-app-statedmi-main OpenCDSS clean, build, create local install (no setup.exe)***.
The console shows an error:

```
-compile-java:
    [mkdir] Created dir: C:\Users\sam\cdss-dev\StateDMI\git-repos\cdss-app-statedmi-main\bin
    [javac] Compiling 624 source files to C:\Users\sam\cdss-dev\StateDMI\git-repos\cdss-app-statedmi-main\bin
    [javac] The system is out of resources.
    [javac] Consult the following stack trace for details.
    [javac] java.lang.StackOverflowError
    [javac] 	at com.sun.tools.javac.util.SharedNameTable.fromChars(SharedNameTable.java:97)
    [javac] 	at com.sun.tools.javac.util.Names.fromChars(Names.java:332)
    [javac] 	at com.sun.tools.javac.parser.UnicodeReader.name(UnicodeReader.java:147)
    [javac] 	at com.sun.tools.javac.parser.JavaTokenizer.scanOperator(JavaTokenizer.java:436)
    [javac] 	at com.sun.tools.javac.parser.JavaTokenizer.readToken(JavaTokenizer.java:651)
    [javac] 	at com.sun.tools.javac.parser.Scanner.nextToken(Scanner.java:115)
```

Similar TSTool code (in `cdss-lib-processor-ts` has no issue with over 845 source iles related to commands).

There are some properties in TSTool `conf/product.properties` and `conf/product-cdss.properties`,
but these seem related to the NSIS installer, and worked before.

I shut down Chrome and the Task Manager is showing 4.4 of 7.9 GB of memory used.
I also compiled TSTool using similar external tools configuration and it worked.

I started Chrome and compiled TSTool and it works.

The 624 source files in StateDMI main are the main StateDMI files and commands copied from TSTool (`rti/tscommandprocessor`).

Some searching indicates that the issue is in calling Ant `javac`, which is in `cdss-util-buildtools/common.xml`
in the `javacompile` macro.  This uses `fork.javac`, which is initialized in `common.xml` `-init-props` target
as `true`.  This causes `javac` to be called as a forked process and also enables a number of properties such as
`memoryMaximumSize`.  See the
[Ant javac documentation](https://ant.apache.org/manual/Tasks/javac.html).
A search for `java.fork` in the StateDMI workspace indicates other instances:

* `cdss-util-buildtools/nbproject` - not used since for NetBeans
* `cdss-util-buildtools/setup/project-template.xml` - not used since apparently for NetBeans
* `src/rti/build/ant/ReloadTask.java` in function `reload()` - sets to `false` - need to search to see if called.

In summary, a value of `fork.java=true` seems to be used.
