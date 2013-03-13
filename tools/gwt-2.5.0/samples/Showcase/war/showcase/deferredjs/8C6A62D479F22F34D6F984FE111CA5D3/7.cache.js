function cCc(){dCc.call(this,false)}
function ACc(a,b){CCc.call(this,a,false);this.c=b}
function BCc(a,b){CCc.call(this,a,false);zCc(this,b)}
function DCc(a){CCc.call(this,'GWT',true);zCc(this,a)}
function n0b(a){this.d=a;this.c=tMb(this.d.b)}
function IBc(a,b){return PBc(a,b,a.b.c)}
function Pb(a,b){$b((ie(),de),a,Ukb(mHb,C1c,135,[(lSc(),b?kSc:jSc)]))}
function zCc(a,b){a.e=b;!!a.d&&bCc(a.d,a);if(b){b.db.tabIndex=-1;yf();Pb(a.db,true)}else{yf();Pb(a.db,false)}}
function PBc(a,b,c){if(c<0||c>a.b.c){throw new cSc}a.p&&(b.db[Cad]=2,undefined);HBc(a,c,b.db);CYc(a.b,c,b);return b}
function pMb(a){var b,c;b=clb(a.b.ie($ad),149);if(b==null){c=Ukb(rHb,D1c,1,['New','Open',_ad,abd,'Exit']);a.b.ke($ad,c);return c}else{return b}}
function oMb(a){var b,c;b=clb(a.b.ie(Zad),149);if(b==null){c=Ukb(rHb,D1c,1,['Undo','Redo','Cut','Copy','Paste']);a.b.ke(Zad,c);return c}else{return b}}
function sMb(a){var b,c;b=clb(a.b.ie(dbd),149);if(b==null){c=Ukb(rHb,D1c,1,['Contents','Fortune Cookie','About GWT']);a.b.ke(dbd,c);return c}else{return b}}
function rMb(a){var b,c;b=clb(a.b.ie(cbd),149);if(b==null){c=Ukb(rHb,D1c,1,['Download','Examples',j7c,"GWT wit' the program"]);a.b.ke(cbd,c);return c}else{return b}}
function qMb(a){var b,c;b=clb(a.b.ie(bbd),149);if(b==null){c=Ukb(rHb,D1c,1,['Fishing in the desert.txt','How to tame a wild parrot','Idiots Guide to Emu Farms']);a.b.ke(bbd,c);return c}else{return b}}
function GCc(){var a;Ni(this,$doc.createElement(I8c));this.db[x4c]='gwt-MenuItemSeparator';a=$doc.createElement(C4c);yoc(this.db,a);a[x4c]='menuSeparatorInner'}
function tMb(a){var b,c;b=clb(a.b.ie(ebd),149);if(b==null){c=Ukb(rHb,D1c,1,['Thank you for selecting a menu item','A fine selection indeed',"Don't you have anything better to do than select menu items?",'Try something else','this is just a menu!','Another wasted click']);a.b.ke(ebd,c);return c}else{return b}}
function j0b(a){var b,c,d,e,f,g,i,j,k,n,o,p,q;o=new n0b(a);n=new cCc;n.c=true;n.db.style[y4c]='500px';n.f=true;q=new dCc(true);p=qMb(a.b);for(k=0;k<p.length;++k){GBc(q,new ACc(p[k],o))}d=new dCc(true);d.f=true;GBc(n,new BCc('File',d));e=pMb(a.b);for(k=0;k<e.length;++k){if(k==3){IBc(d,new GCc);GBc(d,new BCc(e[3],q));IBc(d,new GCc)}else{GBc(d,new ACc(e[k],o))}}b=new dCc(true);GBc(n,new BCc('Edit',b));c=oMb(a.b);for(k=0;k<c.length;++k){GBc(b,new ACc(c[k],o))}f=new dCc(true);GBc(n,new DCc(f));g=rMb(a.b);for(k=0;k<g.length;++k){GBc(f,new ACc(g[k],o))}i=new dCc(true);IBc(n,new GCc);GBc(n,new BCc('Help',i));j=sMb(a.b);for(k=0;k<j.length;++k){GBc(i,new ACc(j[k],o))}fLc(n.db,Y3c,fbd);aCc(n,fbd);return n}
var fbd='cwMenuBar',Zad='cwMenuBarEditOptions',$ad='cwMenuBarFileOptions',bbd='cwMenuBarFileRecents',cbd='cwMenuBarGWTOptions',dbd='cwMenuBarHelpOptions',ebd='cwMenuBarPrompts';tIb(745,1,{},n0b);_.oc=function o0b(){opc(this.c[this.b]);this.b=(this.b+1)%this.c.length};_.b=0;_.d=null;tIb(746,1,q2c);_.mc=function s0b(){YKb(this.c,j0b(this.b))};tIb(1140,102,F1c,cCc);tIb(1147,103,{100:1,105:1,119:1},ACc,BCc,DCc);tIb(1148,103,{100:1,106:1,119:1},GCc);var Ewb=GSc(A9c,'CwMenuBar$1',745),qCb=GSc(y9c,'MenuItemSeparator',1148);d3c(wn)(7);