{
<#if pbill_record.id?has_content>"id" : ${pbill_record.id},</#if>
"owner_num" : "${(pbill_record.owner_num)!}",
"peer_num" : "${(pbill_record.peer_num)!}",
"peer_short_num" : "${(pbill_record.peer_short_num)!}",
"peer_num_type" : ${(pbill_record.peer_num_type)!"-1"},
"peer_num_attr" : "${(pbill_record.peer_num_attr)!}",
"peer_num_isp" : "${(pbill_record.peer_num_isp)!}",
"ven" : ${(pbill_record.ven)!"-1"},
"bill_type" : ${(pbill_record.bill_type)!"-1"},
"started_at" : "${(pbill_record.started_at)!}",
"ended_at" : "${(pbill_record.ended_at)!}",
"weekday" : ${(pbill_record.weekday)!"-1"},
"started_day" : "${(pbill_record.started_day)!}",
"alyz_day" : "${(pbill_record.alyz_day)!}",
"alyz_day_type" : ${(pbill_record.alyz_day_type)!"-1"},
"started_time" : "${(pbill_record.started_time)!}",
"started_time_l1_class" : ${(pbill_record.started_time_l1_class)!"-1"},
"started_time_l2_class" : ${(pbill_record.started_time_l2_class)!"-1"},
"started_hour_class" : ${(pbill_record.started_hour_class)!"-1"},
"time_class" : ${(pbill_record.time_class)!"-1"},
"duration" : ${(pbill_record.duration)!"-1"},
"duration_class" : ${(pbill_record.duration_class)!"-1"},
"comm_direction" : ${(pbill_record.comm_direction)!"-1"},
"owner_num_status" : ${(pbill_record.owner_num_status)!"-1"},
"owner_comm_loc" : "${(pbill_record.owner_comm_loc)!}",
"peer_comm_loc" : "${(pbill_record.peer_comm_loc)!}",
"long_dist" : ${(pbill_record.long_dist)!"-1"},
"owner_cname": "${(pbill_record.owner_cname)!}",
"peer_cname": "${(pbill_record.peer_cname)!}",
<#if pbill_record.owner_citizen_id?has_content>"owner_citizen_id": "${(pbill_record.owner_citizen_id)!}",</#if>
<#if pbill_record.peer_citizen_id?has_content>"peer_citizen_id": "${(pbill_record.peer_citizen_id)!}",</#if>
"owner_mnc" : "${(pbill_record.owner_mnc)!}",
"owner_lac" : "${(pbill_record.owner_lac)!}",
"owner_ci" : "${(pbill_record.owner_ci)!}",
<#if pbill_record.owner_ct_id?has_content>"owner_ct_id" : "${(pbill_record.owner_ct_id)!}",</#if>
<#if pbill_record.peer_ct_code?has_content>"peer_ct_code" : "${(pbill_record.peer_ct_code)!}",</#if>
"owner_ct_code" : "${(pbill_record.owner_ct_code)!}",
"highlight" : ${pbill_record.highlight?then("true", "false")}
}