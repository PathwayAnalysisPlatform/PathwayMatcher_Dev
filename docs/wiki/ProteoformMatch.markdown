# Proteoform matching

Proteoform matching is the process of deciding if two proteoforms are equivalent of each other. 

## Matching types

The matching types defined for PathwayMatcher are:

* [superset](#superset) 
* [subset](#subset)
* [one](#one)
* [superset_no_types](#superset)
* [subset_no_types](#subset)
* [one_no_types](#one)
* [strict](#strict)

The matching type is specified using the command line argument __-m__ or __--matchType__ with the desired
matching type:
~~~~
java -jar PathwayMatcher.jar -t proteoform -m superset -i myFile.txt
~~~~

~~~~
java -jar PathwayMatcher.jar -t proteoform -m strict -i myFile.txt
~~~~

### Superset

The set of input PTMs are a superset of the reference PTMs set

Command line argument: __-m superset__ or __-m superset_no_types__ <br>

* The UniProt Accession is the same
* The Isoform is the same; either:
   * Both have an isoform specified. Ex: P31749-3 
   * Both refer to the default one. Ex: P31749 
* The PTMs:
   * The input contains ALL the reference PTMs or more (Input is superset or equal). 
   Each reference PTM must have a matching input PTM. Some input
   PTMs might not have a matching reference PTM.  
* A PTM matches if this two requirements are true:
   * The types match:
      * If chosen _superset_ then types should be equal
      * If chosen _superset_no_types_ the type is not considered
   * The coordinates match if any this happens:
      * Both are known (positive integer) coordinates and are the same.
      *	Both are known (positive integer) coordinates and they are different, but the absolute difference between the two coordinates is less than or equal to a predefined margin.
      * One of the coordinates is unknown ("null", empty, "?", “-1”). 


### Subset

The set of input PTMs are a subset of the reference PTMs set

Command line argument: __-m subset__ or __-m subset_no_types__ <br>

* The UniProt Accession is the same
* The Isoform is the same; either:
   * Both have an isoform specified. Ex: P31749-3 
   * Both refer to the default one. Ex: P31749 
* The PTMs:
   * Each input PTM must have a matching reference PTM. Some reference PTMs might not have 
   a matching input PTM.  
* A PTM matches if this two requirements are true:
   * The types match:
         * If chosen _subset_ then types should be equal
         * If chosen _subset_no_types_ the type is not considered
   * The coordinates match if any this happens:
      * Both are known (positive integer) coordinates and are the same.
      *	Both are known (positive integer) coordinates and they are different, but the absolute difference between the two coordinates is less than or equal to a predefined margin.
      * One of the coordinates is unknown ("null", empty, "?", “-1”). 

### One

At least one input ptms matches
Command line argument: __-m one__ or __-m one_no_types__ <br>

* The UniProt Accession is the same
* The Isoform is the same; either:
   * Both have an isoform specified. Ex: P31749-3 
   * Both refer to the default one. Ex: P31749 
* The PTMs:
   * At least one input PTM must have a matching reference PTM. 
* A PTM matches if this two requirements are true:
   * The types match:
      * If chosen _one_ then types should be equal
      * If chosen _one_no_types_ the type is not considered
   * The coordinates match if any this happens:
      * Both are known (positive integer) coordinates and are the same.
      *	Both are known (positive integer) coordinates and they are different, but the absolute difference between the two coordinates is less than or equal to a predefined margin.
      * One of the coordinates is unknown ("null", empty, "?", “-1”). 

### Strict

Proteoforms must match exactly in all the attributes.

Command line argument: __-m strict__  <br>

* The UniProt Accession is the same
* The Isoform is the same; either:
   *	Both have an isoform specified. Ex: P31749-3 
   * 	Both refer to the default one. Ex: P31749 
* The PTMs have the same elements:
   * The reference PTM set and the input PTM set have the same size.
   * Each reference PTM has a matching input PTM.
* A PTM matches if:
   * Types are the same.
   * Coordinates are the same:
      * In case they are numbers the should be equal
      * In case they are null, then both should be null.

### Extra considerations:
* Negative values, zero or floating-point numbers are invalid as sequence coordinates in the input.
* We accept only PSI-MOD ontology modification types.
* The margin to compare the coordinates should be set as an unsigned integer.

Table 1 show examples of PTM coordinates matching. The letter _k_ represents any positive integer. 
It compares a PTM coordinate in an input PTM with a PTM coordinate in reference PTM.

__Table 1__

| Input | Reference | Margin | Matched | Comment | 
| --- | --- |--- | --- | --- | 
| 17 | 17 | 0 | Yes | Equal |
| 16 | 17 | 0 |	No | Out of margin |
| 18 | 17 |	0 |	No | Out of margin |
| 7	| 13 | 5 | No |	Out of margin |
| 8	| 13 | 5 | Yes | In margin |
| 9	| 13 | 5 | Yes | In margin |
| 17 | 13 | 5 |	Yes | In margin |
| 18 | 13 | 5 | Yes | In margin |
| 19 | 13 |	5 |	No | Out of margin |
| 0 | 2 | 5 | No | Input in margin but not valid |
| -1 | 2 | 5 | No |	Input in margin but negative |
|?, empty, null | Positive integer | k | Yes | Input is less specific |
| Positive integer | ?, empty, null, -1 | k | Yes | Input is more specific |
|?, empty, null | ?, empty, null, -1 | k | Yes | Equally unspecific |
| Negative int, zero | Any | k | No | Negative or zero input are invalid |


