 <@content for="ext">
   <#if meet?has_content>
    {
      <#if meet.rule == "lacOrCi">
        <#assign lacOrCiMap = meet.lacOrCiMap />
        <#list lacOrCiMap as day,lacOrCiList>
          "${day}" : [
                        <#list lacOrCiList as lacOrCis>
                            "${lacOrCis}"<#sep>,</#sep>
                        </#list>
                     ]<#sep>,</#sep> 
        </#list>
      <#elseif meet.rule == "dist">
        <#assign ctMap = meet.closedCellTowers />
        <#list ctMap as day, ctsList>
          <#assign merged = mergeIfOverlap(ctsList) /> 
          "${day}" : [
                        <#list merged as cts>
                        [
                          <#list cts as ct>
                            "${ct}"<#sep>,</#sep>
                          </#list>
                        ]<#sep>,</#sep>
                        </#list>
                     ]<#sep>,</#sep>              
        </#list>
      </#if>
     }
   </#if>
  </@content>
[
  <#if meet?has_content>
    <#if meet.pbillRecords?has_content>
     <@render partial="/pb/pbill_records/pbill_record" collection=meet.pbillRecords spacer="/common/comma"/>
    </#if>        
  </#if>
]