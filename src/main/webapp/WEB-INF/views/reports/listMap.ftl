[
  <#if listMap?has_content>
    <#list listMap as map>
      {
        <#list map?keys as itemKey>
              <#if itemKey == "owner_ct_code" && map[itemKey]?has_content>
                <#assign cellTower = getCellTower(map[itemKey])!>
                "owner_ct_addr" : "${cellTower.addr!}",
                "coord" : [
                  <#if (cellTower.lng)?has_content>
                  ${cellTower.lng}, ${cellTower.lat}
                  </#if>
                ],
              </#if>
              "${itemKey}" : "${map[itemKey]!}"<#sep>, </#sep>
        </#list>
      }<#sep>, </#sep>
    </#list>
  </#if>
]