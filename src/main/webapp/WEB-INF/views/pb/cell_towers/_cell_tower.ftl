{
"code" : "${(cell_tower.code)!}",
"ci": ${(cell_tower.ci)!""},
"mnc": ${(cell_tower.mnc)!},
"mcc": ${(cell_tower.mcc)!460},
"lac": ${(cell_tower.lac)!""},
<#if cell_tower.lat?has_content>
"lat" : ${(cell_tower.lat)!},
"lng" : ${(cell_tower.lng)!},
</#if>
<#if cell_tower.glat?has_content>
"glat" : ${(cell_tower.glat)!},
"glng" : ${(cell_tower.glng)!},
</#if>
<#if cell_tower.blat?has_content>
"blat" : ${(cell_tower.blat)!},
"blng" : ${(cell_tower.blng)!},
</#if>
<#if cell_tower.xlat?has_content>
"xlat" : ${(cell_tower.xlat)!},
"xlng" : ${(cell_tower.xlng)!},
"xaddr": "${(cell_tower.xaddr)!}",
</#if>
"addr": "${(cell_tower.addr)!}",
"province": "${(cell_tower.province)!}",
"city": "${(cell_tower.city)!}",
"district": "${(cell_tower.district)!}",
"town": "${(cell_tower.town)!}"
}