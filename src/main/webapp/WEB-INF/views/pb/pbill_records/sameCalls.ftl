[
<#if listMap?has_content>
<#list listMap as map>
{
  <#list map?keys as itemKey>
     <#if itemKey == 'owner_num_status'>
        <#assign ownerNumStatus = statColHeader.ownerNumStatus("${map[itemKey]!}")! />
        "${itemKey}" : "${ownerNumStatus}"<#sep>, </#sep>
        <#elseif itemKey == 'comm_direction'>
          <#assign commDirection = statColHeader.commDirection("${map[itemKey]!}")! />
          "${itemKey}" : "${commDirection}"<#sep>, </#sep>
        <#elseif itemKey == 'bill_type'>
          <#assign billType = statColHeader.billType("${map[itemKey]!}")! />
          "${itemKey}" : "${billType}"<#sep>, </#sep>      
        <#elseif itemKey == 'started_time_l1_class'>
          <#assign startedTimeL1 = statColHeader.startedTimeL1("${map[itemKey]!}")! />
          "${itemKey}" : "${startedTimeL1}"<#sep>, </#sep>
        <#elseif itemKey == 'duration_class'>
          <#assign durationClass = statColHeader.durationClass("${map[itemKey]!}")! />
          "${itemKey}" : "${durationClass}"<#sep>, </#sep>
        <#elseif itemKey == 'weekday'>
          <#assign weekday = statColHeader.week("${map[itemKey]!}")! />
          "${itemKey}" : "${weekday}"<#sep>, </#sep>
        <#else>
          "${itemKey}" : "${map[itemKey]!}"<#sep>, </#sep>
     </#if>
  </#list>
}<#sep>, </#sep>
</#list>
</#if>
]