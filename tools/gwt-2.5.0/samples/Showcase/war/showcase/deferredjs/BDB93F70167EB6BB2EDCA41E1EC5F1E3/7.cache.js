function AXb(){BXb.call(this,false)}
function YXb(a,b){$Xb.call(this,a,false);this.b=b}
function ZXb(a,b){$Xb.call(this,a,false);XXb(this,b)}
function _Xb(a){$Xb.call(this,'GWT',true);XXb(this,a)}
function ylb(a){this.c=a;this.b=E5(this.c.a)}
function eXb(a,b){return lXb(a,b,a.a.b)}
function Pb(a,b){$b((ie(),de),a,EH(s0,lnc,135,[(Wbc(),b?Vbc:Ubc)]))}
function XXb(a,b){a.d=b;!!a.c&&zXb(a.c,a);if(b){b.cb.tabIndex=-1;yf();Pb(a.cb,true)}else{yf();Pb(a.cb,false)}}
function lXb(a,b,c){if(c<0||c>a.a.b){throw new Nbc}a.o&&(b.cb[Kvc]=2,undefined);dXb(a,c,b.cb);lic(a.a,c,b);return b}
function cYb(){var a;Ni(this,yr($doc,Otc));this.cb[gqc]='gwt-MenuItemSeparator';a=yr($doc,mqc);aKb(this.cb,a);a[gqc]='menuSeparatorInner'}
function A5(a){var b,c;b=OH(a.a.ld(gwc),149);if(b==null){c=EH(x0,mnc,1,['New','Open',hwc,iwc,'Exit']);a.a.nd(gwc,c);return c}else{return b}}
function z5(a){var b,c;b=OH(a.a.ld(fwc),149);if(b==null){c=EH(x0,mnc,1,['Undo','Redo','Cut','Copy','Paste']);a.a.nd(fwc,c);return c}else{return b}}
function D5(a){var b,c;b=OH(a.a.ld(lwc),149);if(b==null){c=EH(x0,mnc,1,['Contents','Fortune Cookie','About GWT']);a.a.nd(lwc,c);return c}else{return b}}
function C5(a){var b,c;b=OH(a.a.ld(kwc),149);if(b==null){c=EH(x0,mnc,1,['Download','Examples',tsc,"GWT wit' the program"]);a.a.nd(kwc,c);return c}else{return b}}
function B5(a){var b,c;b=OH(a.a.ld(jwc),149);if(b==null){c=EH(x0,mnc,1,['Fishing in the desert.txt','How to tame a wild parrot','Idiots Guide to Emu Farms']);a.a.nd(jwc,c);return c}else{return b}}
function E5(a){var b,c;b=OH(a.a.ld(mwc),149);if(b==null){c=EH(x0,mnc,1,['Thank you for selecting a menu item','A fine selection indeed',"Don't you have anything better to do than select menu items?",'Try something else','this is just a menu!','Another wasted click']);a.a.nd(mwc,c);return c}else{return b}}
function ulb(a){var b,c,d,e,f,g,i,j,k,n,o,p,q;o=new ylb(a);n=new AXb;n.b=true;n.cb.style[hqc]='500px';n.e=true;q=new BXb(true);p=B5(a.a);for(k=0;k<p.length;++k){cXb(q,new YXb(p[k],o))}d=new BXb(true);d.e=true;cXb(n,new ZXb('File',d));e=A5(a.a);for(k=0;k<e.length;++k){if(k==3){eXb(d,new cYb);cXb(d,new ZXb(e[3],q));eXb(d,new cYb)}else{cXb(d,new YXb(e[k],o))}}b=new BXb(true);cXb(n,new ZXb('Edit',b));c=z5(a.a);for(k=0;k<c.length;++k){cXb(b,new YXb(c[k],o))}f=new BXb(true);cXb(n,new _Xb(f));g=C5(a.a);for(k=0;k<g.length;++k){cXb(f,new YXb(g[k],o))}i=new BXb(true);eXb(n,new cYb);cXb(n,new ZXb('Help',i));j=D5(a.a);for(k=0;k<j.length;++k){cXb(i,new YXb(j[k],o))}H4b(n.cb,Hpc,nwc);yXb(n,nwc);return n}
var nwc='cwMenuBar',fwc='cwMenuBarEditOptions',gwc='cwMenuBarFileOptions',jwc='cwMenuBarFileRecents',kwc='cwMenuBarGWTOptions',lwc='cwMenuBarHelpOptions',mwc='cwMenuBarPrompts';z1(660,1,{},ylb);_.nc=function zlb(){RKb(this.b[this.a]);this.a=(this.a+1)%this.b.length};_.a=0;_.c=null;z1(661,1,_nc);_.lc=function Dlb(){h4(this.b,ulb(this.a))};z1(1056,102,onc,AXb);z1(1063,103,{100:1,105:1,119:1},YXb,ZXb,_Xb);z1(1064,103,{100:1,106:1,119:1},cYb);var IR=pcc(Iuc,'CwMenuBar$1',660),vX=pcc(Guc,'MenuItemSeparator',1064);Ooc(vn)(7);