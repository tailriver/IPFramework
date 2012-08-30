RM=rm -f
JAVA=java
JAVAC=javac

DBFILE=foo.db

SQLiteJDBC_JAR=$(HOME)/Documents/lib/SQLiteJDBC-3.7.2/sqlite-jdbc-3.7.2.jar
CLASSPATH=.:$(SQLiteJDBC_JAR)
JAVAFLAGS=-cp $(CLASSPATH):bin
JAVACFLAGS=-d bin -sourcepath src -cp $(CLASSPATH) -deprecation

CLASSES=$(patsubst src/%.java,bin/%.class,$(wildcard src/*.java))

.PHONY: SQLiteSample clean
.SUFFIXES : .java .class

ALL:: model

model: bin/Model.class $(CLASSES)
	$(JAVA) $(JAVAFLAGS) $(<:bin/%.class=%) $(DBFILE) data/model.txt

bin/%.class: src/%.java
	mkdir -p bin
	$(JAVAC) $(JAVACFLAGS) $<

clean:
	$(RM) -R bin $(DBFILE)
