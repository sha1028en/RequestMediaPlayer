# RequestMediaPlayer
Music Player on Android Device

MP3 풀레이어를 내맘대로 구현, 가능한 상용 애플리케이션의 기능을 모방하였다.
## 레이아웃은 다크모드를 기준으로 함.

<hr>

### 액티비티
- MainActivity
  - 일단 퍼미션을 확인하고 없다면 외부 파일 읽기 요청... 거부하면 나머지 작업 일괄 중단.
  - DB가 없다면 컨텐츠 리졸버에게 음악 파일들의 정보를 요청하고 DB에 INSERT한다
  - DB가 있다면 음원 정보를 DB에 요청한다.
  - 음원 정보를 RecyclerView에 등록하고 클릭시 해당 음원 정보들을 들고 MediaEnhancedActivity로 이동한다.
  - 음원을 길게 누르면 해당 음원을 DB에서 삭제하고 RecyclerView에서도 삭제한다.
  - 음원 검색, 즐겨찾기, 전체보기 등 필터링을 지원한다.

  - RecyclerAdapter
    - MainActivity의 Recycyler를 담당한다.
    - 음원 정보들은 목록으로 표시하고 관리한다.
    - item 클릭시 관련 정보를 싸들고 EnhancedActivity로 이동한다

- MediaEnhancedActivity
  - MainActivity에서 받아온 음원정보들을 담아서 리스트로 나열.
  - 리스트에 있는 음원 정보를 바탕으로 음악을 재생한다.
  - ViewPager를 통해 이전 / 다음 음악으로 넘어 갈 수 있다.
  - Seekbar는 음악 재생시간과 동기화 되어 있고, Seekbar를 끌어서 음악 재생시간을 바꿀 수 있다.
  - 종료시 자원들을 반납한다.

  - ViewPagerAdapter
    - MediaEnhancedActivity의 ViewPager를 담당한다.
    - 각 음원의 이미지, 작가명, 파일명을 관리한다.
    - 뷰페이저의 드래그 이벤트를 기반으로 이전 / 다음 음악선택을 구현.

  - MediaViewPagerFragment
    - MediaEnhancedActivity의 ViewPager의 일부이다.
    - 여기서 음원에 알맞는 작가명, 파일명, 커버아트를 표시한다.
    - 재생하는 음원이 변경돼면 같이 바뀐다.
  
<hr>

### DB 모듈
 
- MediaRawItem
  - DB 쿼리 결과나 쿼리시 사용할 전용 데이터 클래스이다.
  - 음악ID, 파일명, 작곡가, 음악정보, 재생시간, 선호여부를 담는다.

- DBOpenHelper
  - 실제 SQLite를 담당하는 클래스.
  - 질의의 결과를 MediaRawItem객체로 담아 준다.
  - 질의를 받을때는 MediaRawItem 또는 음악ID, 작곡가, 파일명( String)으로 받는다.

<hr>

### 음원 모듈
  - MediaPlay
    - 실제 음악의 정보를 담는다.
    - Parcelize처리 되어 있다.
    - 이것을 기반으로 MediaEnhancedActivity의 MediaPlayer가 음악을 재생한다
    - 음원들 다룰때 편한 메서드를 담는다.

