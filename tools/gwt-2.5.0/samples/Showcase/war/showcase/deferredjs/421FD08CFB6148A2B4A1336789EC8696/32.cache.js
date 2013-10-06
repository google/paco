function YF(){YF=r2c;XF=new W_c}
function Bzc(a,b,c,d){var e;a.a.Yg(b,c);e=Dzc(a.a.i,b,c);cj(e,d,true)}
function ZF(d,a){var b=d.a;for(var c in b){b.hasOwnProperty(c)&&a.ne(c)}}
function _F(d,a){a=String(a);var b=d.a;var c=b[a];(c==null||!b.hasOwnProperty(a))&&d.Rd(a);return String(c)}
function bG(){YF();var a;a=tlb(XF.ie(Med),61);if(!a){a=new aG;XF.ke(Med,a)}return a}
function $F(c,b){try{typeof $wnd[b]!='object'&&dG(b);c.a=$wnd[b]}catch(a){dG(b)}}
function dG(a){throw new X0c(a7c+a+"' is not a JavaScript object and cannot be used as a Dictionary")}
function aG(){this.b='Dictionary userInfo';$F(this,Med);if(!this.a){throw new X0c("Cannot find JavaScript object with the name 'userInfo'")}}
function m_b(){var a,b,c,d,e,f,g,i,j,k,n;f=new pMc;g=new Swc('<pre>var userInfo = {\n&nbsp;&nbsp;name: "Amelie Crutcher",\n&nbsp;&nbsp;timeZone: "EST",\n&nbsp;&nbsp;userID: "123",\n&nbsp;&nbsp;lastLogOn: "2/2/2006"\n};<\/pre>\n');g.cb.dir=w6c;g.cb.style['textAlign']=Q6c;mMc(f,new Swc('<b>This example interacts with the following JavaScript variable:<\/b>'));mMc(f,g);j=new tzc;b=j.j;i=bG();e=(n=new c0c,ZF(i,n),n);a=0;for(d=$Yc(vN(e.a));d.a.te();){c=tlb(eZc(d),1);k=_F(i,c);kzc(j,0,a,c);Bzc(b,0,a,'cw-DictionaryExample-header');kzc(j,1,a,k);Bzc(b,1,a,'cw-DictionaryExample-data');++a}mMc(f,new Swc('<br><br>'));mMc(f,j);return f}
var Med='userInfo';LIb(365,1,{61:1},aG);_.Rd=function cG(a){var b;b="Cannot find '"+a+"' in "+this;throw new X0c(b)};_.tS=function eG(){return this.b};_.a=null;_.b=null;var XF;LIb(727,1,l3c);_.lc=function s_b(){tLb(this.a,m_b())};var _qb=BTc(Had,i8c,365);$3c(vn)(32);