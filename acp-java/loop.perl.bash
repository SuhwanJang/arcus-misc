#!/bin/bash

touch loop.perl.log
while :
do 
#  perl yskim.pl 11215 11216 >& repl_test.log
  perl run_all_repl_test.pl 11215 11216 all_kill compare >& repl_test.log
  echo `date` >> loop.perl.log
  echo "perl run_all_repl_test.pl 11215 11216 all_kill compare" >> loop.perl.log
  grep "Finished" repl_test.log >> loop.perl.log
  mv repl_test.log repl_test.all_kill.log

  perl run_all_repl_test.pl 11215 11216 slave_kill compare >& repl_test.log
  echo `date` >> loop.perl.log
  echo "perl run_all_repl_test.pl 11215 11216 slave_kill compare" >> loop.perl.log
  grep "Finished" repl_test.log >> loop.perl.log
  mv repl_test.log repl_test.slave_kill.log

  perl run_all_repl_test.pl 11215 11216 master_kill compare >& repl_test.log
  echo `date` >> loop.perl.log
  echo "perl run_all_repl_test.pl 11215 11216 master_kill compare" >> loop.perl.log
  grep "Finished" repl_test.log >> loop.perl.log
  mv repl_test.log repl_test.master_kill.log

  perl run_all_repl_test.pl 11215 11216 all_stop compare >& repl_test.log
  echo `date` >> loop.perl.log
  echo "perl run_all_repl_test.pl 11215 11216 all_stop compare" >> loop.perl.log
  grep "Finished" repl_test.log >> loop.perl.log
  mv repl_test.log repl_test.all_stop.log

  perl run_all_repl_test.pl 11215 11216 slave_stop compare >& repl_test.log
  echo `date` >> loop.perl.log
  echo "perl run_all_repl_test.pl 11215 11216 slave_stop compare" >> loop.perl.log
  grep "Finished" repl_test.log >> loop.perl.log
  mv repl_test.log repl_test.slave_stop.log

  perl run_all_repl_test.pl 11215 11216 master_stop compare >& repl_test.log
  echo `date` >> loop.perl.log
  echo "perl run_all_repl_test.pl 11215 11216 master_stop compare" >> loop.perl.log
  grep "Finished" repl_test.log >> loop.perl.log
  mv repl_test.log repl_test.master_stop.log

  perl run_all_repl_test.pl 11215 11216 switchover compare >& repl_test.log
  echo `date` >> loop.perl.log
  echo "perl run_all_repl_test.pl 11215 11216 switchover compare" >> loop.perl.log
  grep "Finished" repl_test.log >> loop.perl.log
  mv repl_test.log repl_test.switchover.log
done
