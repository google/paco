function HXb(){IXb.call(this,false)}
function dYb(a,b){fYb.call(this,a,false);this.c=b}
function eYb(a,b){fYb.call(this,a,false);cYb(this,b)}
function gYb(a){fYb.call(this,'GWT',true);cYb(this,a)}
function Plb(a){this.d=a;this.c=W5(this.d.b)}
function lXb(a,b){return sXb(a,b,a.b.c)}
function $b(a,b){jc((te(),oe),a,UH(P0,rnc,135,[(_bc(),b?$bc:Zbc)]))}
function cYb(a,b){a.e=b;!!a.d&&GXb(a.d,a);if(b){b.db.tabIndex=-1;Jf();$b(a.db,true)}else{Jf();$b(a.db,false)}}
function sXb(a,b,c){if(c<0||c>a.b.c){throw new Sbc}a.p&&(b.db[Wvc]=2,undefined);kXb(a,c,b.db);qic(a.b,c,b);return b}
function jYb(){var a;Yi(this,$doc.createElement(buc));this.db[mqc]='gwt-MenuItemSeparator';a=$doc.createElement(rqc);bKb(this.db,a);a[mqc]='menuSeparatorInner'}
function U5(a){var b,c;b=cI(a.b.kd(vwc),149);if(b==null){c=UH(U0,snc,1,['\u4E0B\u8F7D',ysc,Asc,'GWT \u9AD8\u624B\u7A0B\u5E8F']);a.b.md(vwc,c);return c}else{return b}}
function V5(a){var b,c;b=cI(a.b.kd(wwc),149);if(b==null){c=UH(U0,snc,1,['\u5185\u5BB9','\u5E78\u8FD0\u997C','\u5173\u4E8EGWT']);a.b.md(wwc,c);return c}else{return b}}
function R5(a){var b,c;b=cI(a.b.kd(rwc),149);if(b==null){c=UH(U0,snc,1,['\u64A4\u6D88','\u91CD\u590D','\u526A\u5207','\u590D\u5236','\u7C98\u8D34']);a.b.md(rwc,c);return c}else{return b}}
function S5(a){var b,c;b=cI(a.b.kd(swc),149);if(b==null){c=UH(U0,snc,1,['\u65B0\u5EFA','\u6253\u5F00',twc,'\u8FD1\u671F\u6587\u4EF6','\u9000\u51FA']);a.b.md(swc,c);return c}else{return b}}
function T5(a){var b,c;b=cI(a.b.kd(uwc),149);if(b==null){c=UH(U0,snc,1,['Fishing in the desert.txt','How to tame a wild parrot','Idiots Guide to Emu Farms']);a.b.md(uwc,c);return c}else{return b}}
function W5(a){var b,c;b=cI(a.b.kd(xwc),149);if(b==null){c=UH(U0,snc,1,['\u611F\u8C22\u60A8\u9009\u62E9\u83DC\u5355\u9879','\u9009\u5F97\u5F88\u4E0D\u9519','\u9664\u4E86\u9009\u62E9\u83DC\u5355\u9879\u4E4B\u5916\u96BE\u9053\u6CA1\u6709\u66F4\u597D\u7684\u9009\u62E9\uFF1F','\u8BD5\u8BD5\u522B\u7684\u5427','\u8FD9\u4E0D\u8FC7\u662F\u4E2A\u83DC\u5355\u800C\u5DF2\uFF01','\u53C8\u6D6A\u8D39\u4E86\u4E00\u6B21\u70B9\u51FB']);a.b.md(xwc,c);return c}else{return b}}
function Llb(a){var b,c,d,e,f,g,i,j,k,n,o,p,q;o=new Plb(a);n=new HXb;n.c=true;n.db.style[nqc]='500px';n.f=true;q=new IXb(true);p=T5(a.b);for(k=0;k<p.length;++k){jXb(q,new dYb(p[k],o))}d=new IXb(true);d.f=true;jXb(n,new eYb('\u6587\u4EF6',d));e=S5(a.b);for(k=0;k<e.length;++k){if(k==3){lXb(d,new jYb);jXb(d,new eYb(e[3],q));lXb(d,new jYb)}else{jXb(d,new dYb(e[k],o))}}b=new IXb(true);jXb(n,new eYb('\u7F16\u8F91',b));c=R5(a.b);for(k=0;k<c.length;++k){jXb(b,new dYb(c[k],o))}f=new IXb(true);jXb(n,new gYb(f));g=U5(a.b);for(k=0;k<g.length;++k){jXb(f,new dYb(g[k],o))}i=new IXb(true);lXb(n,new jYb);jXb(n,new eYb('\u5E2E\u52A9',i));j=V5(a.b);for(k=0;k<j.length;++k){jXb(i,new dYb(j[k],o))}K4b(n.db,Npc,ywc);FXb(n,ywc);return n}
var ywc='cwMenuBar',rwc='cwMenuBarEditOptions',swc='cwMenuBarFileOptions',uwc='cwMenuBarFileRecents',vwc='cwMenuBarGWTOptions',wwc='cwMenuBarHelpOptions',xwc='cwMenuBarPrompts';W1(662,1,{},Plb);_.sc=function Qlb(){SKb(this.c[this.b]);this.b=(this.b+1)%this.c.length};_.b=0;_.d=null;W1(663,1,foc);_.qc=function Ulb(){z4(this.c,Llb(this.b))};W1(1058,104,unc,HXb);W1(1065,105,{100:1,105:1,119:1},dYb,eYb,gYb);W1(1066,105,{100:1,106:1,119:1},jYb);var eS=ucc(Wuc,'CwMenuBar$1',662),SX=ucc(Uuc,'MenuItemSeparator',1066);Uoc(In)(7);