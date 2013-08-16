function MCc(){NCc.call(this,false)}
function iDc(a,b){kDc.call(this,a,false);this.b=b}
function jDc(a,b){kDc.call(this,a,false);hDc(this,b)}
function lDc(a){kDc.call(this,'GWT',true);hDc(this,a)}
function K0b(a){this.c=a;this.b=QMb(this.c.a)}
function qCc(a,b){return xCc(a,b,a.a.b)}
function Pb(a,b){$b((ie(),de),a,jlb(EHb,x2c,136,[(gTc(),b?fTc:eTc)]))}
function hDc(a,b){a.d=b;!!a.c&&LCc(a.c,a);if(b){b.cb.tabIndex=-1;yf();Pb(a.cb,true)}else{yf();Pb(a.cb,false)}}
function xCc(a,b,c){if(c<0||c>a.a.b){throw new ZSc}a.o&&(b.cb[ubd]=2,undefined);pCc(a,c,b.cb);xZc(a.a,c,b);return b}
function oDc(){var a;Ni(this,yr($doc,y9c));this.cb[s5c]='gwt-MenuItemSeparator';a=yr($doc,y5c);mpc(this.cb,a);a[s5c]='menuSeparatorInner'}
function MMb(a){var b,c;b=tlb(a.a.ie(Sbd),150);if(b==null){c=jlb(JHb,y2c,1,['New','Open',Tbd,Ubd,'Exit']);a.a.ke(Sbd,c);return c}else{return b}}
function LMb(a){var b,c;b=tlb(a.a.ie(Rbd),150);if(b==null){c=jlb(JHb,y2c,1,['Undo','Redo','Cut','Copy','Paste']);a.a.ke(Rbd,c);return c}else{return b}}
function PMb(a){var b,c;b=tlb(a.a.ie(Xbd),150);if(b==null){c=jlb(JHb,y2c,1,['Contents','Fortune Cookie','About GWT']);a.a.ke(Xbd,c);return c}else{return b}}
function OMb(a){var b,c;b=tlb(a.a.ie(Wbd),150);if(b==null){c=jlb(JHb,y2c,1,['Download','Examples',d8c,"GWT wit' the program"]);a.a.ke(Wbd,c);return c}else{return b}}
function NMb(a){var b,c;b=tlb(a.a.ie(Vbd),150);if(b==null){c=jlb(JHb,y2c,1,['Fishing in the desert.txt','How to tame a wild parrot','Idiots Guide to Emu Farms']);a.a.ke(Vbd,c);return c}else{return b}}
function QMb(a){var b,c;b=tlb(a.a.ie(Ybd),150);if(b==null){c=jlb(JHb,y2c,1,['Thank you for selecting a menu item','A fine selection indeed',"Don't you have anything better to do than select menu items?",'Try something else','this is just a menu!','Another wasted click']);a.a.ke(Ybd,c);return c}else{return b}}
function G0b(a){var b,c,d,e,f,g,i,j,k,n,o,p,q;o=new K0b(a);n=new MCc;n.b=true;n.cb.style[t5c]='500px';n.e=true;q=new NCc(true);p=NMb(a.a);for(k=0;k<p.length;++k){oCc(q,new iDc(p[k],o))}d=new NCc(true);d.e=true;oCc(n,new jDc('File',d));e=MMb(a.a);for(k=0;k<e.length;++k){if(k==3){qCc(d,new oDc);oCc(d,new jDc(e[3],q));qCc(d,new oDc)}else{oCc(d,new iDc(e[k],o))}}b=new NCc(true);oCc(n,new jDc('Edit',b));c=LMb(a.a);for(k=0;k<c.length;++k){oCc(b,new iDc(c[k],o))}f=new NCc(true);oCc(n,new lDc(f));g=OMb(a.a);for(k=0;k<g.length;++k){oCc(f,new iDc(g[k],o))}i=new NCc(true);qCc(n,new oDc);oCc(n,new jDc('Help',i));j=PMb(a.a);for(k=0;k<j.length;++k){oCc(i,new iDc(j[k],o))}TLc(n.cb,T4c,Zbd);KCc(n,Zbd);return n}
var Zbd='cwMenuBar',Rbd='cwMenuBarEditOptions',Sbd='cwMenuBarFileOptions',Vbd='cwMenuBarFileRecents',Wbd='cwMenuBarGWTOptions',Xbd='cwMenuBarHelpOptions',Ybd='cwMenuBarPrompts';LIb(747,1,{},K0b);_.nc=function L0b(){bqc(this.b[this.a]);this.a=(this.a+1)%this.b.length};_.a=0;_.c=null;LIb(748,1,l3c);_.lc=function P0b(){tLb(this.b,G0b(this.a))};LIb(1143,102,A2c,MCc);LIb(1150,103,{101:1,106:1,120:1},iDc,jDc,lDc);LIb(1151,103,{101:1,107:1,120:1},oDc);var Uwb=BTc(sad,'CwMenuBar$1',747),HCb=BTc(qad,'MenuItemSeparator',1151);$3c(vn)(7);