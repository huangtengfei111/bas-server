<#if sizeAndColor?has_content>
<#assign r = sizeAndColor._1() />
<#assign color = sizeAndColor._2() />
<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" height="${r*2}" width="${r*2}">
  <circle cx="${r}" cy="${r}" r="${r}" stroke="${color}" stroke-width="0" fill="${color}" /> 
</svg> 
</#if>