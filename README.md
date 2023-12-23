

Solves White jigsaw puzzles, inspired by the video by Stuff Made Here:
https://www.youtube.com/watch?v=Gu_1S77XkiM and https://www.youtube.com/watch?v=WsPHBD5NsS0 


| Formulation                 | Number of Variables | Number of Clauses |
|-----------------------------|---------------------|-------------------|
| [1](https://mathb.in/77183) | n^2 + 4n            | 48n^4 + n^2 + n   |
| [2](https://mathb.in/77190) | n^2 + 6n            | 16n^2 + 4n        |
| [3](https://mathb.in/77209) | 26n                 | O(n log(n))       |

Long story short. Formulation 1 10x10 was fast enough to run in a few minutes, 
13x13 took hours and 100s of GB of memory. Formulation 2, 13x13 takes around 30 seconds,
largest I can run is about 18x18. Formulation 3, I can run 32x32 in just 2-3 seconds. I
think we have finally reached the limit of this. 64x64 runs in 283 seconds. 70x72 (5040 piece)
puzzle took about 9 minutes.

See a sample result of 13x13 in [RUNS](./RUNS.md).

It also possible to use this to generate a DIMACS cnf file, I implemented a variant of 
[Tseitin Transform](https://en.wikipedia.org/wiki/Tseytin_transformation) to make this, and performance
using MiniSAT is worse than using OR Tools, but scales better. 

```shell
brew install maven 
mvn package
java -jar target/jigsaw-1.0-SNAPSHOT.jar --random --M=5 --N=5
```

Sample Output:
```
Using random jigsaw puzzle
Argument --sat_solver_path=[path] missing, using Google OR Tools
Source diagram:
   -      -      -
|     <<     <<     |
   V      ^      V
   V      ^      V
|     >>     <<     |
   V      ^      ^
   V      ^      ^
|     <<     <<     |
   -      -      -

Reconstituted Diagram:
   -      -      -
|     <<     <<     |
   ^      V      V
   ^      V      V
|     >>     <<     |
   ^      V      ^
   ^      V      ^
|     >>     >>     |
   -      -      -

1. (1,0,1) -> (0,1,2)
2. (0,1,3) -> (2,1,3)
3. (2,1,0) -> (1,0,3)
4. (2,0,3) -> (0,0,0)
5. (1,1,0) -> (1,1,2)
6. (2,2,0) -> (2,0,3)
7. (0,2,3) -> (0,2,1)
8. (1,2,1) -> (1,2,3)
9. (0,0,2) -> (2,2,0)
```

You can also use any SAT solver that accepts cnf files. 
```shell
java -jar target/jigsaw-1.0-SNAPSHOT.jar \
  --random --M=3 --N=3 \
  --sat_solver_path=/Users/anita/bin/bin/minisat
```

```
Using random jigsaw puzzle
Source diagram:
   -      -      -
|     >>     >>     |
   ^      ^      V
   ^      ^      V
|     >>     >>     |
   V      ^      ^
   V      ^      ^
|     >>     <<     |
   -      -      -

File Written
Waiting for concat
File Concatenated
Waiting for miniSAT
============================[ Problem Statistics ]=============================
|                                                                             |
|  Number of variables:           315                                         |
|  Number of clauses:           12483                                         |
|  Parse time:                   0.00 s                                       |
|  Eliminated clauses:           0.00 Mb                                      |
|  Simplification time:          0.01 s                                       |
|                                                                             |
============================[ Search Statistics ]==============================
| Conflicts |          ORIGINAL         |          LEARNT          | Progress |
|           |    Vars  Clauses Literals |    Limit  Clauses Lit/Cl |          |
===============================================================================
===============================================================================
restarts              : 1
conflicts             : 0              (0 /sec)
decisions             : 11             (0.00 % random) (1098 /sec)
propagations          : 234            (23358 /sec)
conflict literals     : 0              ( nan % deleted)
Memory used           : 5.76 MB
CPU time              : 0.010018 s

SATISFIABLE
miniSAT done
Reading solution
Done parsing solution
Reconstituted Diagram:
   -      -      -
|     >>     >>     |
   ^      ^      V
   ^      ^      V
|     >>     >>     |
   V      ^      ^
   V      ^      ^
|     >>     <<     |
   -      -      -

1. (1,1,3) -> (1,1,1)
2. (0,0,2) -> (0,2,1)
3. (1,2,0) -> (1,2,0)
4. (2,2,2) -> (2,2,2)
5. (0,1,3) -> (0,1,1)
6. (0,2,3) -> (0,0,2)
7. (2,0,2) -> (2,0,2)
8. (2,1,3) -> (2,1,1)
9. (1,0,0) -> (1,0,0)
```

* TODO: Take custom user pieces instead of random.
* TODO: Use better SAT solvers like Glucose.
* TODO: Submit the problems to SAT competition 2023.

