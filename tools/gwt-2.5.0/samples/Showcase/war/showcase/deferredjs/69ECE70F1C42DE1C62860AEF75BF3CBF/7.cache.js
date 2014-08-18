function x8b(){y8b.call(this,false)}
function V8b(a,b){X8b.call(this,a,false);this.c=b}
function W8b(a,b){X8b.call(this,a,false);U8b(this,b)}
function Y8b(a){X8b.call(this,'GWT',true);U8b(this,a)}
function Fyb(a){this.d=a;this.c=Lib(this.d.b)}
function b8b(a,b){return i8b(a,b,a.b.c)}
function $b(a,b){jc((te(),oe),a,pU(Edb,hAc,135,[(Roc(),b?Qoc:Poc)]))}
function U8b(a,b){a.e=b;!!a.d&&w8b(a.d,a);if(b){b.db.tabIndex=-1;Jf();$b(a.db,true)}else{Jf();$b(a.db,false)}}
function i8b(a,b,c){if(c<0||c>a.b.c){throw new Ioc}a.p&&(b.db[PIc]=2,undefined);a8b(a,c,b.db);gvc(a.b,c,b);return b}
function Gib(a){var b,c;b=zU(a.b.me(kJc),149);if(b==null){c=pU(Jdb,iAc,1,[lJc,'R\xE9tablir','Couper','Copier','Coller']);a.b.oe(kJc,c);return c}else{return b}}
function Hib(a){var b,c;b=zU(a.b.me(mJc),149);if(b==null){c=pU(Jdb,iAc,1,['Nouveau','Ouvrir',nJc,'R\xE9cent','Quitter']);a.b.oe(mJc,c);return c}else{return b}}
function Kib(a){var b,c;b=zU(a.b.me(qJc),149);if(b==null){c=pU(Jdb,iAc,1,['Contenu','Biscuit de fortune','\xC0 propos de GWT']);a.b.oe(qJc,c);return c}else{return b}}
function Jib(a){var b,c;b=zU(a.b.me(pJc),149);if(b==null){c=pU(Jdb,iAc,1,['T\xE9l\xE9charger','Exemples',wFc,'GWiTtez avec le programme']);a.b.oe(pJc,c);return c}else{return b}}
function Iib(a){var b,c;b=zU(a.b.me(oJc),149);if(b==null){c=pU(Jdb,iAc,1,['P\xEAcher dans le d\xE9sert.txt','Comment apprivoiser un perroquet sauvage',"L'\xE9levage des \xE9meus pour les nuls"]);a.b.oe(oJc,c);return c}else{return b}}
function _8b(){var a;Yi(this,$doc.createElement(YGc));this.db[cDc]='gwt-MenuItemSeparator';a=$doc.createElement(hDc);TWb(this.db,a);a[cDc]='menuSeparatorInner'}
function Lib(a){var b,c;b=zU(a.b.me(rJc),149);if(b==null){c=pU(Jdb,iAc,1,["Merci d'avoir s\xE9lectionn\xE9 une option de menu",'Une s\xE9lection vraiment pertinente',"N'avez-vous rien de mieux \xE0 faire que de s\xE9lectionner des options de menu?","Essayez quelque chose d'autre","ceci n'est qu'un menu!",'Un autre clic gaspill\xE9']);a.b.oe(rJc,c);return c}else{return b}}
function Byb(a){var b,c,d,e,f,g,i,j,k,n,o,p,q;o=new Fyb(a);n=new x8b;n.c=true;n.db.style[dDc]='500px';n.f=true;q=new y8b(true);p=Iib(a.b);for(k=0;k<p.length;++k){_7b(q,new V8b(p[k],o))}d=new y8b(true);d.f=true;_7b(n,new W8b('Fichier',d));e=Hib(a.b);for(k=0;k<e.length;++k){if(k==3){b8b(d,new _8b);_7b(d,new W8b(e[3],q));b8b(d,new _8b)}else{_7b(d,new V8b(e[k],o))}}b=new y8b(true);_7b(n,new W8b('\xC9dition',b));c=Gib(a.b);for(k=0;k<c.length;++k){_7b(b,new V8b(c[k],o))}f=new y8b(true);_7b(n,new Y8b(f));g=Jib(a.b);for(k=0;k<g.length;++k){_7b(f,new V8b(g[k],o))}i=new y8b(true);b8b(n,new _8b);_7b(n,new W8b('Aide',i));j=Kib(a.b);for(k=0;k<j.length;++k){_7b(i,new V8b(j[k],o))}Ahc(n.db,DCc,sJc);v8b(n,sJc);return n}
var sJc='cwMenuBar',kJc='cwMenuBarEditOptions',mJc='cwMenuBarFileOptions',oJc='cwMenuBarFileRecents',pJc='cwMenuBarGWTOptions',qJc='cwMenuBarHelpOptions',rJc='cwMenuBarPrompts';Leb(684,1,{},Fyb);_.sc=function Gyb(){IXb(this.c[this.b]);this.b=(this.b+1)%this.c.length};_.b=0;_.d=null;Leb(685,1,XAc);_.qc=function Kyb(){ohb(this.c,Byb(this.b))};Leb(1080,104,kAc,x8b);Leb(1087,105,{100:1,105:1,119:1},V8b,W8b,Y8b);Leb(1088,105,{100:1,106:1,119:1},_8b);var V2=kpc(RHc,'CwMenuBar$1',684),H8=kpc(PHc,'MenuItemSeparator',1088);KBc(In)(7);