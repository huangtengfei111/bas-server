[
  <#if listMap?has_content>
    <#list listMap as map>
      {
        <#list map?keys as itemKey>
              <#if itemKey == "owner_ct_code" && map[itemKey]?has_content>
                <#assign cellTower = getCellTower(map[itemKey])!>
                <#if cellTower?has_content>
                  "coord" : [${cellTower.lng!},${cellTower.lat!}],
                </#if>
              </#if>
              "${itemKey}" : "${map[itemKey]!}"<#sep>, </#sep>
        </#list>
      }<#sep>, </#sep>
    </#list>
  </#if>
]