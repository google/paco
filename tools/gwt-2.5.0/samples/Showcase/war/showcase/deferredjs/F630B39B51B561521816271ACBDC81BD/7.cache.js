function xXb(){yXb.call(this,false)}
function VXb(a,b){XXb.call(this,a,false);this.c=b}
function WXb(a,b){XXb.call(this,a,false);UXb(this,b)}
function YXb(a){XXb.call(this,'GWT',true);UXb(this,a)}
function Flb(a){this.d=a;this.c=L5(this.d.b)}
function bXb(a,b){return iXb(a,b,a.b.c)}
function $b(a,b){jc((te(),oe),a,OH(E0,hnc,134,[(Rbc(),b?Qbc:Pbc)]))}
function UXb(a,b){a.e=b;!!a.d&&wXb(a.d,a);if(b){b.db.tabIndex=-1;Jf();$b(a.db,true)}else{Jf();$b(a.db,false)}}
function iXb(a,b,c){if(c<0||c>a.b.c){throw new Ibc}a.p&&(b.db[Pvc]=2,undefined);aXb(a,c,b.db);gic(a.b,c,b);return b}
function H5(a){var b,c;b=YH(a.b.pd(lwc),148);if(b==null){c=OH(J0,inc,1,['New','Open',mwc,nwc,'Exit']);a.b.rd(lwc,c);return c}else{return b}}
function G5(a){var b,c;b=YH(a.b.pd(kwc),148);if(b==null){c=OH(J0,inc,1,['Undo','Redo','Cut','Copy','Paste']);a.b.rd(kwc,c);return c}else{return b}}
function K5(a){var b,c;b=YH(a.b.pd(qwc),148);if(b==null){c=OH(J0,inc,1,['Contents','Fortune Cookie','About GWT']);a.b.rd(qwc,c);return c}else{return b}}
function J5(a){var b,c;b=YH(a.b.pd(pwc),148);if(b==null){c=OH(J0,inc,1,['Download','Examples',psc,"GWT wit' the program"]);a.b.rd(pwc,c);return c}else{return b}}
function I5(a){var b,c;b=YH(a.b.pd(owc),148);if(b==null){c=OH(J0,inc,1,['Fishing in the desert.txt','How to tame a wild parrot','Idiots Guide to Emu Farms']);a.b.rd(owc,c);return c}else{return b}}
function _Xb(){var a;Yi(this,$doc.createElement(Utc));this.db[cqc]='gwt-MenuItemSeparator';a=$doc.createElement(hqc);TJb(this.db,a);a[cqc]='menuSeparatorInner'}
function L5(a){var b,c;b=YH(a.b.pd(rwc),148);if(b==null){c=OH(J0,inc,1,['Thank you for selecting a menu item','A fine selection indeed',"Don't you have anything better to do than select menu items?",'Try something else','this is just a menu!','Another wasted click']);a.b.rd(rwc,c);return c}else{return b}}
function Blb(a){var b,c,d,e,f,g,i,j,k,n,o,p,q;o=new Flb(a);n=new xXb;n.c=true;n.db.style[dqc]='500px';n.f=true;q=new yXb(true);p=I5(a.b);for(k=0;k<p.length;++k){_Wb(q,new VXb(p[k],o))}d=new yXb(true);d.f=true;_Wb(n,new WXb('File',d));e=H5(a.b);for(k=0;k<e.length;++k){if(k==3){bXb(d,new _Xb);_Wb(d,new WXb(e[3],q));bXb(d,new _Xb)}else{_Wb(d,new VXb(e[k],o))}}b=new yXb(true);_Wb(n,new WXb('Edit',b));c=G5(a.b);for(k=0;k<c.length;++k){_Wb(b,new VXb(c[k],o))}f=new yXb(true);_Wb(n,new YXb(f));g=J5(a.b);for(k=0;k<g.length;++k){_Wb(f,new VXb(g[k],o))}i=new yXb(true);bXb(n,new _Xb);_Wb(n,new WXb('Help',i));j=K5(a.b);for(k=0;k<j.length;++k){_Wb(i,new VXb(j[k],o))}A4b(n.db,Dpc,swc);vXb(n,swc);return n}
var swc='cwMenuBar',kwc='cwMenuBarEditOptions',lwc='cwMenuBarFileOptions',owc='cwMenuBarFileRecents',pwc='cwMenuBarGWTOptions',qwc='cwMenuBarHelpOptions',rwc='cwMenuBarPrompts';L1(659,1,{},Flb);_.sc=function Glb(){IKb(this.c[this.b]);this.b=(this.b+1)%this.c.length};_.b=0;_.d=null;L1(660,1,Xnc);_.qc=function Klb(){o4(this.c,Blb(this.b))};L1(1055,104,knc,xXb);L1(1062,105,{99:1,104:1,118:1},VXb,WXb,YXb);L1(1063,105,{99:1,105:1,118:1},_Xb);var VR=kcc(Nuc,'CwMenuBar$1',659),HX=kcc(Luc,'MenuItemSeparator',1063);Koc(In)(7);