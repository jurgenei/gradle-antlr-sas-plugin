data work.out;
  retain transaction_id grid_id;
  length module_code $25.;
  set work.in;
  if rating = "" then delete;
run;

proc sql;
  create table work.final as
  select a.transaction_id, a.grid_id, coalesce(a.module_code, "no_data") as module_code
  from work.out as a;
quit;

