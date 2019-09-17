[
  <#if listMap?has_content>
    <#list listMap as map>
      <#if map.peer_num_total?has_content>
        {"peer_num_total" : "${map.peer_num_total}"}<#sep>,</#sep>
      <#else>
        {
          "owner_num" : [
                        <#if map.owner_nums?has_content > 
                           <#list map.owner_nums as owner_num>      
                              "${owner_num}"<#sep>,</#sep>
                            </#list> 
                        </#if>
                      ],
          "peer_num" : "${map.peer_num}",
          "correlation" : "${map.correlation}",
          "long_time_calls" : "${map.long_time_calls!}",
          "correlation_total" : "${getNumConnection(caseId, map.peer_num)!}",
          "long_time_call_total" : "${map.long_time_call_total}",
          "peer_num_attr" : "${map.peer_num_attr!}",
          "first_start" : "${map.first_start}",
          "last_start" : "${map.last_start}"
        }<#sep>,</#sep>
        </#if>
    </#list>
  </#if>
]