<@content for="ext">
  <#if ownerCtCodeRepeat?has_content>
    <#list ownerCtCodeRepeat as ownerCtCode>
      "${ownerCtCode!}"<#sep>,</#sep>
    </#list>
  </#if>
</@content>
[
  <#if groupByNum>
    <#if ownerCtLHM?has_content>
      <#list ownerCtLHM as lhmKey, lhmVal>
        {
          "${lhmKey}": [  
              <#list lhmVal as ls>
                {
                  <#list ls as k, v>
                    <#if k == "owner_ct_code">
                    "count" : ${count[v]!},
                    </#if>
                     "${k}" : "${v!}"<#sep>, </#sep>
                  </#list>
                }<#sep>,</#sep> 
              </#list>
           ]
        }<#sep>, </#sep>
      </#list>
    </#if>
  <#else>
    <#if ownerCtLM?has_content>
      <#list ownerCtLM as ownerCtM>
        {
          <#list ownerCtM as k, v>
            <#if k == "owner_ct_code">
            "count" : ${count[v]!},
            </#if>
             "${k}" : "${v!}"<#sep>, </#sep>
          </#list>
        }<#sep>,</#sep> 
      </#list>
    </#if>
  </#if>

]