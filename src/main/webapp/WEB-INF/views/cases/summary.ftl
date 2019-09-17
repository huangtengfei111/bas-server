[{
    "name" : "${case.name!''}",
    "num" : "${case.num!null}",
    "started_at" : "${case.started_at!''}",
    "ended_at" : "${case.ended_at!''}",
    "operator" : "${case.operator!''}",
    "pb_started_at" : "${case.pb_started_at!''}",
    "pb_ended_at" : "${case.pb_ended_at!''}",
    "pb_alyz_day_start" : "${case.pb_alyz_day_start!''}",
    "pb_alyz_day_end" : "${case.pb_alyz_day_end!''}",
    "owner_num_count" : "${case.owner_num_count!''}",
    "peer_num_count" : "${case.peer_num_count!''}",
    "pb_rec_count" : "${case.pb_rec_count!''}",
    "status" : "${case.status!''}",
    "memo" : "${case.memo!''}",
    "created_by" : "${case.created_by!''}",
    "created_at" : "${case.created_at!''}",
    "updated_at" : "${case.updated_at!''}",
    "jobs" : [<#list jobs as job>      
              {
                "jid" : "${job.jid!''}",
                "jtype" : "${job.jtype!''}",
                "executed_at" : "${job.executed_at!''}",
                "ended_at" : "${job.ended_at!''}",
                "created_at" : "${job.created_at!''}"
              }<#sep>,</#sep>
               </#list> 
             ]
  }
]
