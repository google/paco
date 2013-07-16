function uCc(){vCc.call(this,false)}
function SCc(a,b){UCc.call(this,a,false);this.b=b}
function TCc(a,b){UCc.call(this,a,false);RCc(this,b)}
function VCc(a){UCc.call(this,'GWT',true);RCc(this,a)}
function z0b(a){this.c=a;this.b=FMb(this.c.a)}
function $Bc(a,b){return fCc(a,b,a.a.b)}
function Pb(a,b){$b((ie(),de),a,$kb(tHb,d2c,136,[(OSc(),b?NSc:MSc)]))}
function RCc(a,b){a.d=b;!!a.c&&tCc(a.c,a);if(b){b.cb.tabIndex=-1;yf();Pb(a.cb,true)}else{yf();Pb(a.cb,false)}}
function fCc(a,b,c){if(c<0||c>a.a.b){throw new FSc}a.o&&(b.cb[_ad]=2,undefined);ZBc(a,c,b.cb);dZc(a.a,c,b);return b}
function BMb(a){var b,c;b=ilb(a.a.ie(xbd),150);if(b==null){c=$kb(yHb,e2c,1,['New','Open',ybd,zbd,'Exit']);a.a.ke(xbd,c);return c}else{return b}}
function AMb(a){var b,c;b=ilb(a.a.ie(wbd),150);if(b==null){c=$kb(yHb,e2c,1,['Undo','Redo','Cut','Copy','Paste']);a.a.ke(wbd,c);return c}else{return b}}
function EMb(a){var b,c;b=ilb(a.a.ie(Cbd),150);if(b==null){c=$kb(yHb,e2c,1,['Contents','Fortune Cookie','About GWT']);a.a.ke(Cbd,c);return c}else{return b}}
function DMb(a){var b,c;b=ilb(a.a.ie(Bbd),150);if(b==null){c=$kb(yHb,e2c,1,['Download','Examples',J7c,"GWT wit' the program"]);a.a.ke(Bbd,c);return c}else{return b}}
function CMb(a){var b,c;b=ilb(a.a.ie(Abd),150);if(b==null){c=$kb(yHb,e2c,1,['Fishing in the desert.txt','How to tame a wild parrot','Idiots Guide to Emu Farms']);a.a.ke(Abd,c);return c}else{return b}}
function YCc(){var a;Ni(this,$doc.createElement(e9c));this.cb[$4c]='gwt-MenuItemSeparator';a=$doc.createElement(d5c);Poc(this.cb,a);a[$4c]='menuSeparatorInner'}
function FMb(a){var b,c;b=ilb(a.a.ie(Dbd),150);if(b==null){c=$kb(yHb,e2c,1,['Thank you for selecting a menu item','A fine selection indeed',"Don't you have anything better to do than select menu items?",'Try something else','this is just a menu!','Another wasted click']);a.a.ke(Dbd,c);return c}else{return b}}
function v0b(a){var b,c,d,e,f,g,i,j,k,n,o,p,q;o=new z0b(a);n=new uCc;n.b=true;n.cb.style[_4c]='500px';n.e=true;q=new vCc(true);p=CMb(a.a);for(k=0;k<p.length;++k){YBc(q,new SCc(p[k],o))}d=new vCc(true);d.e=true;YBc(n,new TCc('File',d));e=BMb(a.a);for(k=0;k<e.length;++k){if(k==3){$Bc(d,new YCc);YBc(d,new TCc(e[3],q));$Bc(d,new YCc)}else{YBc(d,new SCc(e[k],o))}}b=new vCc(true);YBc(n,new TCc('Edit',b));c=AMb(a.a);for(k=0;k<c.length;++k){YBc(b,new SCc(c[k],o))}f=new vCc(true);YBc(n,new VCc(f));g=DMb(a.a);for(k=0;k<g.length;++k){YBc(f,new SCc(g[k],o))}i=new vCc(true);$Bc(n,new YCc);YBc(n,new TCc('Help',i));j=EMb(a.a);for(k=0;k<j.length;++k){YBc(i,new SCc(j[k],o))}BLc(n.cb,z4c,Ebd);sCc(n,Ebd);return n}
var Ebd='cwMenuBar',wbd='cwMenuBarEditOptions',xbd='cwMenuBarFileOptions',Abd='cwMenuBarFileRecents',Bbd='cwMenuBarGWTOptions',Cbd='cwMenuBarHelpOptions',Dbd='cwMenuBarPrompts';AIb(748,1,{},z0b);_.nc=function A0b(){Epc(this.b[this.a]);this.a=(this.a+1)%this.b.length};_.a=0;_.c=null;AIb(749,1,T2c);_.lc=function E0b(){iLb(this.b,v0b(this.a))};AIb(1146,102,g2c,uCc);AIb(1153,103,{101:1,106:1,120:1},SCc,TCc,VCc);AIb(1154,103,{101:1,107:1,120:1},YCc);var Jwb=hTc(Z9c,'CwMenuBar$1',748),wCb=hTc(X9c,'MenuItemSeparator',1154);G3c(wn)(7);