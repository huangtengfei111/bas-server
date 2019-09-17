[
  <#if cell_towers?has_content>
    <#list cell_towers as cell_tower>
     { 
      "ct_code" : "${cell_tower.code}",
      "lac" : "${cell_tower.lac}",
      "ci" : "${cell_tower.ci}",
      "mnc" : "${cell_tower.mnc}",
      "addr" : "${cell_tower.addr}"
     }<#sep>,</#sep>
    </#list>
  </#if>
]