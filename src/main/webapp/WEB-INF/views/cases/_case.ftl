{
"id" : ${case.id},
"name" : "${(case.name)!}",
"num": "${(case.num)!}",
"started_at": "${(case.started_at)!}",
"ended_at": "${(case.ended_at)!}",
"operator": "${(case.operator)!}",
"owner_num_count": ${(case.owner_num_count)!0},
"peer_num_count": ${(case.peer_num_count)!0},
"pb_started_at": "${(case.pb_started_at)!}",
"pb_ended_at": "${(case.pb_ended_at)!}",
"pb_alyz_day_start": "${(case.pb_alyz_day_start)!}",
"pb_alyz_day_end": "${(case.pb_alyz_day_end)!}",
"pb_rec_count": "${(case.pb_rec_count)!0}",
"pb_city" : "${case.pb_city!}",
"status": "${(case.status)!}",
"memo": "${(case.memo)!}",
"jobs": [
           <#if case.caseJobs?has_content > 
            <#list case.caseJobs as job>      
              {
                "jid" : "${job.jid!''}",
                "jtype" : "${job.jtype!''}",
                "executed_at" : "${job.executed_at!''}",
                "ended_at" : "${job.ended_at!''}",
                "created_at" : "${job.created_at!''}"
              }<#sep>,</#sep>
            </#list> 
           </#if>
         ],
"updated_at": "${(case.updated_at)!}"
}