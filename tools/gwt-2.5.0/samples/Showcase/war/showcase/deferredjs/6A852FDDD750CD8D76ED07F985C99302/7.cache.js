function JCc(){KCc.call(this,false)}
function fDc(a,b){hDc.call(this,a,false);this.c=b}
function gDc(a,b){hDc.call(this,a,false);eDc(this,b)}
function iDc(a){hDc.call(this,'GWT',true);eDc(this,a)}
function R0b(a){this.d=a;this.c=XMb(this.d.b)}
function nCc(a,b){return uCc(a,b,a.b.c)}
function $b(a,b){jc((te(),oe),a,tlb(QHb,t2c,135,[(bTc(),b?aTc:_Sc)]))}
function eDc(a,b){a.e=b;!!a.d&&ICc(a.d,a);if(b){b.db.tabIndex=-1;Jf();$b(a.db,true)}else{Jf();$b(a.db,false)}}
function uCc(a,b,c){if(c<0||c>a.b.c){throw new USc}a.p&&(b.db[zbd]=2,undefined);mCc(a,c,b.db);sZc(a.b,c,b);return b}
function TMb(a){var b,c;b=Dlb(a.b.me(Xbd),149);if(b==null){c=tlb(VHb,u2c,1,['New','Open',Ybd,Zbd,'Exit']);a.b.oe(Xbd,c);return c}else{return b}}
function SMb(a){var b,c;b=Dlb(a.b.me(Wbd),149);if(b==null){c=tlb(VHb,u2c,1,['Undo','Redo','Cut','Copy','Paste']);a.b.oe(Wbd,c);return c}else{return b}}
function WMb(a){var b,c;b=Dlb(a.b.me(acd),149);if(b==null){c=tlb(VHb,u2c,1,['Contents','Fortune Cookie','About GWT']);a.b.oe(acd,c);return c}else{return b}}
function VMb(a){var b,c;b=Dlb(a.b.me(_bd),149);if(b==null){c=tlb(VHb,u2c,1,['Download','Examples',_7c,"GWT wit' the program"]);a.b.oe(_bd,c);return c}else{return b}}
function UMb(a){var b,c;b=Dlb(a.b.me($bd),149);if(b==null){c=tlb(VHb,u2c,1,['Fishing in the desert.txt','How to tame a wild parrot','Idiots Guide to Emu Farms']);a.b.oe($bd,c);return c}else{return b}}
function lDc(){var a;Yi(this,$doc.createElement(E9c));this.db[o5c]='gwt-MenuItemSeparator';a=$doc.createElement(t5c);dpc(this.db,a);a[o5c]='menuSeparatorInner'}
function XMb(a){var b,c;b=Dlb(a.b.me(bcd),149);if(b==null){c=tlb(VHb,u2c,1,['Thank you for selecting a menu item','A fine selection indeed',"Don't you have anything better to do than select menu items?",'Try something else','this is just a menu!','Another wasted click']);a.b.oe(bcd,c);return c}else{return b}}
function N0b(a){var b,c,d,e,f,g,i,j,k,n,o,p,q;o=new R0b(a);n=new JCc;n.c=true;n.db.style[p5c]='500px';n.f=true;q=new KCc(true);p=UMb(a.b);for(k=0;k<p.length;++k){lCc(q,new fDc(p[k],o))}d=new KCc(true);d.f=true;lCc(n,new gDc('File',d));e=TMb(a.b);for(k=0;k<e.length;++k){if(k==3){nCc(d,new lDc);lCc(d,new gDc(e[3],q));nCc(d,new lDc)}else{lCc(d,new fDc(e[k],o))}}b=new KCc(true);lCc(n,new gDc('Edit',b));c=SMb(a.b);for(k=0;k<c.length;++k){lCc(b,new fDc(c[k],o))}f=new KCc(true);lCc(n,new iDc(f));g=VMb(a.b);for(k=0;k<g.length;++k){lCc(f,new fDc(g[k],o))}i=new KCc(true);nCc(n,new lDc);lCc(n,new gDc('Help',i));j=WMb(a.b);for(k=0;k<j.length;++k){lCc(i,new fDc(j[k],o))}MLc(n.db,P4c,ccd);HCc(n,ccd);return n}
var ccd='cwMenuBar',Wbd='cwMenuBarEditOptions',Xbd='cwMenuBarFileOptions',$bd='cwMenuBarFileRecents',_bd='cwMenuBarGWTOptions',acd='cwMenuBarHelpOptions',bcd='cwMenuBarPrompts';XIb(746,1,{},R0b);_.sc=function S0b(){Upc(this.c[this.b]);this.b=(this.b+1)%this.c.length};_.b=0;_.d=null;XIb(747,1,h3c);_.qc=function W0b(){ALb(this.c,N0b(this.b))};XIb(1142,104,w2c,JCc);XIb(1149,105,{100:1,105:1,119:1},fDc,gDc,iDc);XIb(1150,105,{100:1,106:1,119:1},lDc);var fxb=wTc(xad,'CwMenuBar$1',746),TCb=wTc(vad,'MenuItemSeparator',1150);W3c(In)(7);