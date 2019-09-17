[
    <#list audit_logs as  audit_log>  
      {
        "id" : "${audit_log.id}",
        "user_id" : "${audit_log.user_id}",
        "subject" : "${audit_log.subject}",
        "remote_host" : "${audit_log.remote_host}",
        "case_id" : "${audit_log.case_id}",
        "action" : "${audit_log.action}",
        "params" : "${audit_log.params?js_string}",
        "created_at" : "${audit_log.created_at}"
      }<#sep>,</#sep>
    </#list> 
]