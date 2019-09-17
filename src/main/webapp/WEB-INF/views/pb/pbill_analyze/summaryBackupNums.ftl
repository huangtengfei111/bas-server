[
  <#if summaryBackupNums?has_content>
    <#list summaryBackupNums as k,v>
      {
        "back_num" : "${v.backupNum}",
        "cname" : "${v.cname!""}",
        "count" : ${v.count},
        "day_count" : ${v.dayCount},
        "work_time_count": ${v.workTimeCount},
        "private_time_count": ${v.privateTimeCount}
      }<#sep>,</#sep>
    </#list>
  </#if>
]
     