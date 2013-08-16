function i8b(){j8b.call(this,false)}
function G8b(a,b){I8b.call(this,a,false);this.b=b}
function H8b(a,b){I8b.call(this,a,false);F8b(this,b)}
function J8b(a){I8b.call(this,'GWT',true);F8b(this,a)}
function nyb(a){this.c=a;this.b=tib(this.c.a)}
function O7b(a,b){return V7b(a,b,a.a.b)}
function Pb(a,b){$b((ie(),de),a,WT(hdb,Tzc,136,[(Coc(),b?Boc:Aoc)]))}
function F8b(a,b){a.d=b;!!a.c&&h8b(a.c,a);if(b){b.cb.tabIndex=-1;yf();Pb(a.cb,true)}else{yf();Pb(a.cb,false)}}
function V7b(a,b,c){if(c<0||c>a.a.b){throw new toc}a.o&&(b.cb[pIc]=2,undefined);N7b(a,c,b.cb);Tuc(a.a,c,b);return b}
function oib(a){var b,c;b=eU(a.a.ie(MIc),150);if(b==null){c=WT(mdb,Uzc,1,[NIc,'R\xE9tablir','Couper','Copier','Coller']);a.a.ke(MIc,c);return c}else{return b}}
function pib(a){var b,c;b=eU(a.a.ie(OIc),150);if(b==null){c=WT(mdb,Uzc,1,['Nouveau','Ouvrir',PIc,'R\xE9cent','Quitter']);a.a.ke(OIc,c);return c}else{return b}}
function sib(a){var b,c;b=eU(a.a.ie(SIc),150);if(b==null){c=WT(mdb,Uzc,1,['Contenu','Biscuit de fortune','\xC0 propos de GWT']);a.a.ke(SIc,c);return c}else{return b}}
function rib(a){var b,c;b=eU(a.a.ie(RIc),150);if(b==null){c=WT(mdb,Uzc,1,['T\xE9l\xE9charger','Exemples',eFc,'GWiTtez avec le programme']);a.a.ke(RIc,c);return c}else{return b}}
function qib(a){var b,c;b=eU(a.a.ie(QIc),150);if(b==null){c=WT(mdb,Uzc,1,['P\xEAcher dans le d\xE9sert.txt','Comment apprivoiser un perroquet sauvage',"L'\xE9levage des \xE9meus pour les nuls"]);a.a.ke(QIc,c);return c}else{return b}}
function M8b(){var a;Ni(this,$doc.createElement(yGc));this.cb[OCc]='gwt-MenuItemSeparator';a=$doc.createElement(TCc);DWb(this.cb,a);a[OCc]='menuSeparatorInner'}
function tib(a){var b,c;b=eU(a.a.ie(TIc),150);if(b==null){c=WT(mdb,Uzc,1,["Merci d'avoir s\xE9lectionn\xE9 une option de menu",'Une s\xE9lection vraiment pertinente',"N'avez-vous rien de mieux \xE0 faire que de s\xE9lectionner des options de menu?","Essayez quelque chose d'autre","ceci n'est qu'un menu!",'Un autre clic gaspill\xE9']);a.a.ke(TIc,c);return c}else{return b}}
function jyb(a){var b,c,d,e,f,g,i,j,k,n,o,p,q;o=new nyb(a);n=new i8b;n.b=true;n.cb.style[PCc]='500px';n.e=true;q=new j8b(true);p=qib(a.a);for(k=0;k<p.length;++k){M7b(q,new G8b(p[k],o))}d=new j8b(true);d.e=true;M7b(n,new H8b('Fichier',d));e=pib(a.a);for(k=0;k<e.length;++k){if(k==3){O7b(d,new M8b);M7b(d,new H8b(e[3],q));O7b(d,new M8b)}else{M7b(d,new G8b(e[k],o))}}b=new j8b(true);M7b(n,new H8b('\xC9dition',b));c=oib(a.a);for(k=0;k<c.length;++k){M7b(b,new G8b(c[k],o))}f=new j8b(true);M7b(n,new J8b(f));g=rib(a.a);for(k=0;k<g.length;++k){M7b(f,new G8b(g[k],o))}i=new j8b(true);O7b(n,new M8b);M7b(n,new H8b('Aide',i));j=sib(a.a);for(k=0;k<j.length;++k){M7b(i,new G8b(j[k],o))}phc(n.cb,nCc,UIc);g8b(n,UIc);return n}
var UIc='cwMenuBar',MIc='cwMenuBarEditOptions',OIc='cwMenuBarFileOptions',QIc='cwMenuBarFileRecents',RIc='cwMenuBarGWTOptions',SIc='cwMenuBarHelpOptions',TIc='cwMenuBarPrompts';oeb(686,1,{},nyb);_.nc=function oyb(){sXb(this.b[this.a]);this.a=(this.a+1)%this.b.length};_.a=0;_.c=null;oeb(687,1,HAc);_.lc=function syb(){Ygb(this.b,jyb(this.a))};oeb(1084,102,Wzc,i8b);oeb(1091,103,{101:1,106:1,120:1},G8b,H8b,J8b);oeb(1092,103,{101:1,107:1,120:1},M8b);var x2=Xoc(rHc,'CwMenuBar$1',686),k8=Xoc(pHc,'MenuItemSeparator',1092);uBc(wn)(7);