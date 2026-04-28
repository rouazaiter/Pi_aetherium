import{a as Q}from"./chunk-CT6ACN7A.js";import{a as H}from"./chunk-44XJSU5A.js";import{a as Y}from"./chunk-IKUIBY4H.js";import"./chunk-RISAZLUV.js";import"./chunk-IQTQC7FU.js";import"./chunk-O2NBSDTX.js";import"./chunk-WB52CYT5.js";import"./chunk-KO2Q2FPL.js";import"./chunk-BX3BNT5R.js";import"./chunk-YHKBR7IS.js";import"./chunk-IJOGFACV.js";import"./chunk-6IVRF7ZQ.js";import"./chunk-OSMOBTQB.js";import{B as J,C as K}from"./chunk-E6NQ4I6I.js";import"./chunk-NWWD7OLB.js";import{N as P,R as I,S as N,T as U,U as V,V as X,W as Z,X as j,Y as q,r as B}from"./chunk-254T6CJ4.js";import"./chunk-5SVGYKV3.js";import"./chunk-5YSU25A5.js";import"./chunk-3HZTE7UG.js";import{b as o,d as h}from"./chunk-VA3WZCDJ.js";import{E as C,H as O,k as L}from"./chunk-EBL6GGQR.js";import{l as G}from"./chunk-PWCKSV3D.js";var ee=B.pie,D={sections:new Map,showData:!1,config:ee},u=D.sections,y=D.showData,he=structuredClone(ee),ue=o(()=>structuredClone(he),"getConfig"),me=o(()=>{u=new Map,y=D.showData,I()},"clear"),ve=o(({label:e,value:a})=>{if(a<0)throw new Error(`"${e}" has invalid value: ${a}. Negative values are not allowed in pie charts. All slice values must be >= 0.`);u.has(e)||(u.set(e,a),h.debug(`added new section: ${e}, with value: ${a}`))},"addSection"),xe=o(()=>u,"getSections"),Se=o(e=>{y=e},"setShowData"),we=o(()=>y,"getShowData"),te={getConfig:ue,clear:me,setDiagramTitle:Z,getDiagramTitle:j,setAccTitle:N,getAccTitle:U,setAccDescription:V,getAccDescription:X,addSection:ve,getSections:xe,setShowData:Se,getShowData:we},Ce=o((e,a)=>{Q(e,a),a.setShowData(e.showData),e.sections.map(a.addSection)},"populateDb"),De={parse:o(e=>G(void 0,null,function*(){let a=yield Y("pie",e);h.debug(a),Ce(a,te)}),"parse")},ye=o(e=>`
  .pieCircle{
    stroke: ${e.pieStrokeColor};
    stroke-width : ${e.pieStrokeWidth};
    opacity : ${e.pieOpacity};
  }
  .pieOuterCircle{
    stroke: ${e.pieOuterStrokeColor};
    stroke-width: ${e.pieOuterStrokeWidth};
    fill: none;
  }
  .pieTitleText {
    text-anchor: middle;
    font-size: ${e.pieTitleTextSize};
    fill: ${e.pieTitleTextColor};
    font-family: ${e.fontFamily};
  }
  .slice {
    font-family: ${e.fontFamily};
    fill: ${e.pieSectionTextColor};
    font-size:${e.pieSectionTextSize};
    // fill: white;
  }
  .legend text {
    fill: ${e.pieLegendTextColor};
    font-family: ${e.fontFamily};
    font-size: ${e.pieLegendTextSize};
  }
`,"getStyles"),$e=ye,Te=o(e=>{let a=[...e.values()].reduce((r,l)=>r+l,0),$=[...e.entries()].map(([r,l])=>({label:r,value:l})).filter(r=>r.value/a*100>=1);return O().value(r=>r.value).sort(null)($)},"createPieArcs"),Ae=o((e,a,$,T)=>{h.debug(`rendering pie chart
`+e);let r=T.db,l=q(),A=K(r.getConfig(),l.pie),b=40,n=18,p=4,s=450,d=s,m=H(a),c=m.append("g");c.attr("transform","translate("+d/2+","+s/2+")");let{themeVariables:i}=l,[E]=J(i.pieOuterStrokeWidth);E??=2;let _=A.textPosition,g=Math.min(d,s)/2-b,ae=C().innerRadius(0).outerRadius(g),ie=C().innerRadius(g*_).outerRadius(g*_);c.append("circle").attr("cx",0).attr("cy",0).attr("r",g+E/2).attr("class","pieOuterCircle");let f=r.getSections(),re=Te(f),oe=[i.pie1,i.pie2,i.pie3,i.pie4,i.pie5,i.pie6,i.pie7,i.pie8,i.pie9,i.pie10,i.pie11,i.pie12],v=0;f.forEach(t=>{v+=t});let k=re.filter(t=>(t.data.value/v*100).toFixed(0)!=="0"),x=L(oe).domain([...f.keys()]);c.selectAll("mySlices").data(k).enter().append("path").attr("d",ae).attr("fill",t=>x(t.data.label)).attr("class","pieCircle"),c.selectAll("mySlices").data(k).enter().append("text").text(t=>(t.data.value/v*100).toFixed(0)+"%").attr("transform",t=>"translate("+ie.centroid(t)+")").style("text-anchor","middle").attr("class","slice");let ne=c.append("text").text(r.getDiagramTitle()).attr("x",0).attr("y",-(s-50)/2).attr("class","pieTitleText"),R=[...f.entries()].map(([t,w])=>({label:t,value:w})),S=c.selectAll(".legend").data(R).enter().append("g").attr("class","legend").attr("transform",(t,w)=>{let M=n+p,pe=M*R.length/2,ge=12*n,fe=w*M-pe;return"translate("+ge+","+fe+")"});S.append("rect").attr("width",n).attr("height",n).style("fill",t=>x(t.label)).style("stroke",t=>x(t.label)),S.append("text").attr("x",n+p).attr("y",n-p).text(t=>r.getShowData()?`${t.label} [${t.value}]`:t.label);let le=Math.max(...S.selectAll("text").nodes().map(t=>t?.getBoundingClientRect().width??0)),se=d+b+n+p+le,W=ne.node()?.getBoundingClientRect().width??0,ce=d/2-W/2,de=d/2+W/2,z=Math.min(0,ce),F=Math.max(se,de)-z;m.attr("viewBox",`${z} 0 ${F} ${s}`),P(m,s,F,A.useMaxWidth)},"draw"),be={draw:Ae},Ge={parser:De,db:te,renderer:be,styles:$e};export{Ge as diagram};
