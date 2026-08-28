%let source_table = raw.sales;

data work.input;
  set &source_table.;
  amount = 42;
run;

proc sql;
  create table &TARGET. as
  select amount from work.input;
quit;

