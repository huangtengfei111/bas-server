{
<#if cell_tower?has_content>
        "${cell_tower.code!}": [
           [${cell_tower.xlng}, ${cell_tower.xlat}],
           {
             "address": "${cell_tower.addr!}"
           }
        ]
</#if>
}
