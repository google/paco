function A8b(){B8b.call(this,false)}
function Y8b(a,b){$8b.call(this,a,false);this.b=b}
function Z8b(a,b){$8b.call(this,a,false);X8b(this,b)}
function _8b(a){$8b.call(this,'GWT',true);X8b(this,a)}
function yyb(a){this.c=a;this.b=Eib(this.c.a)}
function e8b(a,b){return l8b(a,b,a.a.b)}
function Pb(a,b){$b((ie(),de),a,fU(sdb,lAc,136,[(Woc(),b?Voc:Uoc)]))}
function X8b(a,b){a.d=b;!!a.c&&z8b(a.c,a);if(b){b.cb.tabIndex=-1;yf();Pb(a.cb,true)}else{yf();Pb(a.cb,false)}}
function l8b(a,b,c){if(c<0||c>a.a.b){throw new Noc}a.o&&(b.cb[KIc]=2,undefined);d8b(a,c,b.cb);lvc(a.a,c,b);return b}
function c9b(){var a;Ni(this,yr($doc,SGc));this.cb[gDc]='gwt-MenuItemSeparator';a=yr($doc,mDc);aXb(this.cb,a);a[gDc]='menuSeparatorInner'}
function zib(a){var b,c;b=pU(a.a.ie(fJc),150);if(b==null){c=fU(xdb,mAc,1,[gJc,'R\xE9tablir','Couper','Copier','Coller']);a.a.ke(fJc,c);return c}else{return b}}
function Aib(a){var b,c;b=pU(a.a.ie(hJc),150);if(b==null){c=fU(xdb,mAc,1,['Nouveau','Ouvrir',iJc,'R\xE9cent','Quitter']);a.a.ke(hJc,c);return c}else{return b}}
function Dib(a){var b,c;b=pU(a.a.ie(lJc),150);if(b==null){c=fU(xdb,mAc,1,['Contenu','Biscuit de fortune','\xC0 propos de GWT']);a.a.ke(lJc,c);return c}else{return b}}
function Cib(a){var b,c;b=pU(a.a.ie(kJc),150);if(b==null){c=fU(xdb,mAc,1,['T\xE9l\xE9charger','Exemples',AFc,'GWiTtez avec le programme']);a.a.ke(kJc,c);return c}else{return b}}
function Bib(a){var b,c;b=pU(a.a.ie(jJc),150);if(b==null){c=fU(xdb,mAc,1,['P\xEAcher dans le d\xE9sert.txt','Comment apprivoiser un perroquet sauvage',"L'\xE9levage des \xE9meus pour les nuls"]);a.a.ke(jJc,c);return c}else{return b}}
function Eib(a){var b,c;b=pU(a.a.ie(mJc),150);if(b==null){c=fU(xdb,mAc,1,["Merci d'avoir s\xE9lectionn\xE9 une option de menu",'Une s\xE9lection vraiment pertinente',"N'avez-vous rien de mieux \xE0 faire que de s\xE9lectionner des options de menu?","Essayez quelque chose d'autre","ceci n'est qu'un menu!",'Un autre clic gaspill\xE9']);a.a.ke(mJc,c);return c}else{return b}}
function uyb(a){var b,c,d,e,f,g,i,j,k,n,o,p,q;o=new yyb(a);n=new A8b;n.b=true;n.cb.style[hDc]='500px';n.e=true;q=new B8b(true);p=Bib(a.a);for(k=0;k<p.length;++k){c8b(q,new Y8b(p[k],o))}d=new B8b(true);d.e=true;c8b(n,new Z8b('Fichier',d));e=Aib(a.a);for(k=0;k<e.length;++k){if(k==3){e8b(d,new c9b);c8b(d,new Z8b(e[3],q));e8b(d,new c9b)}else{c8b(d,new Y8b(e[k],o))}}b=new B8b(true);c8b(n,new Z8b('\xC9dition',b));c=zib(a.a);for(k=0;k<c.length;++k){c8b(b,new Y8b(c[k],o))}f=new B8b(true);c8b(n,new _8b(f));g=Cib(a.a);for(k=0;k<g.length;++k){c8b(f,new Y8b(g[k],o))}i=new B8b(true);e8b(n,new c9b);c8b(n,new Z8b('Aide',i));j=Dib(a.a);for(k=0;k<j.length;++k){c8b(i,new Y8b(j[k],o))}Hhc(n.cb,HCc,nJc);y8b(n,nJc);return n}
var nJc='cwMenuBar',fJc='cwMenuBarEditOptions',hJc='cwMenuBarFileOptions',jJc='cwMenuBarFileRecents',kJc='cwMenuBarGWTOptions',lJc='cwMenuBarHelpOptions',mJc='cwMenuBarPrompts';zeb(685,1,{},yyb);_.nc=function zyb(){RXb(this.b[this.a]);this.a=(this.a+1)%this.b.length};_.a=0;_.c=null;zeb(686,1,_Ac);_.lc=function Dyb(){hhb(this.b,uyb(this.a))};zeb(1081,102,oAc,A8b);zeb(1088,103,{101:1,106:1,120:1},Y8b,Z8b,_8b);zeb(1089,103,{101:1,107:1,120:1},c9b);var I2=ppc(MHc,'CwMenuBar$1',685),v8=ppc(KHc,'MenuItemSeparator',1089);OBc(vn)(7);