
data work.input;
  set raw.sales;
  amount = 42;
run;

proc sql;
  create table work.sales as
  select amount from work.input;
quit;

