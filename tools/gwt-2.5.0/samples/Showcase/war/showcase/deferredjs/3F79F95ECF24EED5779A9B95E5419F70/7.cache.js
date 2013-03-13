function Q8b(){R8b.call(this,false)}
function m9b(a,b){o9b.call(this,a,false);this.c=b}
function n9b(a,b){o9b.call(this,a,false);l9b(this,b)}
function p9b(a){o9b.call(this,'GWT',true);l9b(this,a)}
function Yyb(a){this.d=a;this.c=cjb(this.d.b)}
function t8b(a,b){return B8b(a,b,a.b.c)}
function _b(a,b){kc((ue(),pe),a,CU(Xdb,FAc,135,[(mpc(),b?lpc:kpc)]))}
function B8b(a,b,c){if(c<0||c>a.b.c){throw new dpc}a.p&&(b.db[lJc]=2,undefined);s8b(a,c,b.db);Evc(a.b,c,b);return b}
function l9b(a,b){a.e=b;!!a.d&&P8b(a.d,a);if(b){(R5b(),b.db).tabIndex=-1;Kf();_b(a.db,true)}else{Kf();_b(a.db,false)}}
function Zib(a){var b,c;b=MU(a.b.qe(IJc),149);if(b==null){c=CU(aeb,GAc,1,[JJc,'R\xE9tablir','Couper','Copier','Coller']);a.b.se(IJc,c);return c}else{return b}}
function $ib(a){var b,c;b=MU(a.b.qe(KJc),149);if(b==null){c=CU(aeb,GAc,1,['Nouveau','Ouvrir',LJc,'R\xE9cent','Quitter']);a.b.se(KJc,c);return c}else{return b}}
function bjb(a){var b,c;b=MU(a.b.qe(OJc),149);if(b==null){c=CU(aeb,GAc,1,['Contenu','Biscuit de fortune','\xC0 propos de GWT']);a.b.se(OJc,c);return c}else{return b}}
function ajb(a){var b,c;b=MU(a.b.qe(NJc),149);if(b==null){c=CU(aeb,GAc,1,['T\xE9l\xE9charger','Exemples',_Fc,'GWiTtez avec le programme']);a.b.se(NJc,c);return c}else{return b}}
function _ib(a){var b,c;b=MU(a.b.qe(MJc),149);if(b==null){c=CU(aeb,GAc,1,['P\xEAcher dans le d\xE9sert.txt','Comment apprivoiser un perroquet sauvage',"L'\xE9levage des \xE9meus pour les nuls"]);a.b.se(MJc,c);return c}else{return b}}
function s9b(){var a;Zi(this,$doc.createElement(uHc));this.db[BDc]='gwt-MenuItemSeparator';a=$doc.createElement(GDc);hXb(this.db,a);a[BDc]='menuSeparatorInner'}
function cjb(a){var b,c;b=MU(a.b.qe(PJc),149);if(b==null){c=CU(aeb,GAc,1,["Merci d'avoir s\xE9lectionn\xE9 une option de menu",'Une s\xE9lection vraiment pertinente',"N'avez-vous rien de mieux \xE0 faire que de s\xE9lectionner des options de menu?","Essayez quelque chose d'autre","ceci n'est qu'un menu!",'Un autre clic gaspill\xE9']);a.b.se(PJc,c);return c}else{return b}}
function Uyb(a){var b,c,d,e,f,g,i,j,k,n,o,p,q;o=new Yyb(a);n=new Q8b;n.c=true;n.db.style[CDc]='500px';n.f=true;q=new R8b(true);p=_ib(a.b);for(k=0;k<p.length;++k){r8b(q,new m9b(p[k],o))}d=new R8b(true);d.f=true;r8b(n,new n9b('Fichier',d));e=$ib(a.b);for(k=0;k<e.length;++k){if(k==3){t8b(d,new s9b);r8b(d,new n9b(e[3],q));t8b(d,new s9b)}else{r8b(d,new m9b(e[k],o))}}b=new R8b(true);r8b(n,new n9b('\xC9dition',b));c=Zib(a.b);for(k=0;k<c.length;++k){r8b(b,new m9b(c[k],o))}f=new R8b(true);r8b(n,new p9b(f));g=ajb(a.b);for(k=0;k<g.length;++k){r8b(f,new m9b(g[k],o))}i=new R8b(true);t8b(n,new s9b);r8b(n,new n9b('Aide',i));j=bjb(a.b);for(k=0;k<j.length;++k){r8b(i,new m9b(j[k],o))}Thc(n.db,aDc,QJc);O8b(n,QJc);return n}
var QJc='cwMenuBar',IJc='cwMenuBarEditOptions',KJc='cwMenuBarFileOptions',MJc='cwMenuBarFileRecents',NJc='cwMenuBarGWTOptions',OJc='cwMenuBarHelpOptions',PJc='cwMenuBarPrompts';cfb(688,1,{},Yyb);_.sc=function Zyb(){ZXb(this.c[this.b]);this.b=(this.b+1)%this.c.length};_.b=0;_.d=null;cfb(689,1,tBc);_.qc=function bzb(){Hhb(this.c,Uyb(this.b))};cfb(1083,104,IAc,Q8b);cfb(1090,105,{100:1,105:1,119:1},m9b,n9b,p9b);cfb(1091,105,{100:1,106:1,119:1},s9b);var k3=Hpc(nIc,'CwMenuBar$1',688),Y8=Hpc(lIc,'MenuItemSeparator',1091);gCc(Jn)(7);