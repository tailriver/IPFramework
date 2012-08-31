RM=rm -f
JAVA=java
JAVAC=javac

DBFILE=foo.db

SQLiteJDBC_JAR=$(HOME)/Documents/lib/SQLiteJDBC-3.7.2/sqlite-jdbc-3.7.2.jar
CLASSPATH=.:$(SQLiteJDBC_JAR)
JAVAFLAGS=-cp $(CLASSPATH):bin
JAVACFLAGS=-d bin -sourcepath src -cp $(CLASSPATH) -deprecation

CLASSES=$(patsubst src/%.java,bin/%.class,$(wildcard src/*.java))

.PHONY: model factor factor_result clean
.SUFFIXES : .java .class

ALL:: factor_result

model: bin/Model.class $(CLASSES)
	$(JAVA) $(JAVAFLAGS) $(<:bin/%.class=%) $(DBFILE) data/model.txt

factor: bin/Factor.class $(CLASSES) model
	$(JAVA) $(JAVAFLAGS) $(<:bin/%.class=%) $(DBFILE) data/factor_maximum_rtz.txt

factor_result: bin/FactorResult.class $(CLASSES) model factor
	$(JAVA) $(JAVAFLAGS) $(<:bin/%.class=%) $(DBFILE) data/factor_result/

bin/%.class: src/%.java
	mkdir -p bin
	$(JAVAC) $(JAVACFLAGS) $<

clean:
	$(RM) -R bin $(DBFILE)
