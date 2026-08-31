data work.flags;
  set work.input;
  if rating ne "no_data" then do;
    status = "active";
  end;
  else status = "unknown";
run;

proc sql;
  create table work.summary as
  select distinct transaction_id,
         case when grid_id = . then 0 else 1 end as in_scope_flag
  from work.flags
  where status not in ("", "NA")
  order by transaction_id;
quit;

