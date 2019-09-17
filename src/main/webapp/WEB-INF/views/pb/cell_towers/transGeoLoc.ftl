{
<#if ctCoords?has_content>
    <#list ctCoords as ctCode, coords>
       <#if (coords?size > 0) >
       <#assign tCoords = coords[0] />
       <#assign ct = coords[1] />
        "${ctCode!}": [
           [${tCoords[0]}, ${tCoords[1]}],
           {
             "address": "${ct.addr!}"
           }
        ]
        <#else>
        "${ctCode!}": []        
        </#if><#sep>,</#sep>
    </#list>
</#if>
}
