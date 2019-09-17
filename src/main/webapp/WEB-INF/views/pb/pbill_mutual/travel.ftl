<@content for="ext">
  <#if ext?has_content>
    {
      <#list ext as loc, timesList>
        "${loc}" : [
                      <#list timesList as times>
                      [
                        <#list times as time>
                          "${time}"<#sep>,</#sep>
                        </#list>
                      ]<#sep>,</#sep>
                      </#list>
                   ]<#sep>,</#sep>              
      </#list>
    }
  </#if>
</@content>

[
  <#if pbill_records?has_content>
    <@render partial="/pb/pbill_records/pbill_record" collection=pbill_records spacer="/common/comma"/>
  </#if>
]