#!/bin/bash

mkdir -p pidfiles

#g0 m-11281 | s-11282
#./integration/run.memcached.bash master 11281
#./integration/run.memcached.bash slave 11282

#g1 m-11283 | s-11284
./integration/run.memcached.bash master 11283
./integration/run.memcached.bash slave 11284

#g2 m-11285 | s-11286
./integration/run.memcached.bash master 11285
./integration/run.memcached.bash slave 11286

#g3 m-11287 | s-11288
./integration/run.memcached.bash master 11287
./integration/run.memcached.bash slave 11288

#g4 m-11289 | s-11290
./integration/run.memcached.bash master 11289
./integration/run.memcached.bash slave 11290

