# jmeter_exts
Proprietary extensions

It provides a JMeter "sampler" that publishes a brand safety Kafka message in each iteration.  Kafka parameters are configurable through GUI.

To use it with JMeter, first build the jar:

```mvn clean package```

The resulting jar is `target/jmeter_exts-1.0-SNAPSHOT-jar-with-dependencies.jar` which packages all the dependencies.

Then copy the jar to the extensions directory of your JMeter installation:

`cp target/jmeter_exts-1.0-SNAPSHOT-jar-with-dependencies.jar <path_to_JMeter>/lib/ext`

The Sampler will be available in its menu next time JMeter is launched.

For load testing, use NON GUI Mode:
`jmeter -n -t [jmx file] -l [results file] -e -o [Path to output folder]`
   
Also, adapt Java Heap to your test requirements:
Modify HEAP="-Xms512m -Xmx512m" in the JMeter batch file
