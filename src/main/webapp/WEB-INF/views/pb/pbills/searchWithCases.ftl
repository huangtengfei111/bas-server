[
<#if map?has_content>
<#list map as case, pbills>
{
 "case": <@render partial="/cases/case" />,
 "pbills": [<@render partial="pbill" collection=pbills spacer="/common/comma"/>]
 }<#sep>,</#sep>
</#list>
</#if>
]