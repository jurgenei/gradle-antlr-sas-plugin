%let target_table = work.sales;
data work.input;
  set raw.sales;
  amount = 42;
run;

proc sql;
  create table &target_table. as
  select amount from work.input;
quit;

