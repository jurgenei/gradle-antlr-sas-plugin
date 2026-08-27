data work.sales_clean keep=id amount drop=temp;
  set work.sales_raw;
  amount = (price * qty) - discount;
  format amount dollar12.2;
run;

proc sql;
  create table work.report as
  select a.id, a.amount + 1 as amount_plus_one
  from work.sales_clean a
  left join work.customer_dim b on a.id = b.id
  where (a.amount > 100 and b.id ^= 0) or a.amount = 42;
quit;

