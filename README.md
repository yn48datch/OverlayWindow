Welcome to the OverlayWindow wiki!

# OverlayWindow

## Summary

OverlayWindow��Android �p�̃��C�u�����ł��B
* Activity����̃��C���[�ŔC�ӂ̃��C�A�E�g�i�A�v���P�[�V�����j��\���B
* ��������
* Activity���������銴�o�ŁA�A�v���P�[�V�����������ł���
��L�̃R���Z�v�g�̌��ŁA�쐬���Ă��܂��B

## How to setup
Welcome to the OverlayWindow wiki!

# OverlayWindow

## Summary

OverlayWindow��Android �p�̃��C�u�����ł��B
* Activity����̃��C���[�ŔC�ӂ̃��C�A�E�g�i�A�v���P�[�V�����j��\���B
* ��������
* Activity���������銴�o�ŁA�A�v���P�[�V�����������ł���
��L�̃R���Z�v�g�̌��ŁA�쐬���Ă��܂��B

## How to setup

�M�҂�Eclipse�ł̊J�������\�z���Ă��邽�߁A
�ꕔ�AEclipse�݂̂ł������Ă͂܂�Ȃ��L�ڂ�����܂��B  

1. Android �A�v���P�[�V�����J�����̍\�z  
   �ȗ����܂��B
   
2. Android �A�v���P�[�V�����v���W�F�N�g�̍쐬(���Ȃ��̃A�v��)  
   �ȗ����܂��B
   
3. OverlayWindow��Library�Ƃ��ēo�^����(��������o���ł��B�B)  
   Eclipse �ŁA �C���|�[�g -> �����v���W�F�N�g�����[�N�X�y�[�X��  
   ��OK���Ǝv���܂��B  
   
4. �g�p�������A�v���P�[�V�����̃v���W�F�N�g��OverlayWindow Library��ǉ�  
   Eclipse �ŁA �A�v���P�[�V�����̃v���W�F�N�g���E�N���b�N  
   �v���p�f�B -> Android -> ���C�u���� �ŁAOverlayWindow ��ǉ�  


## How to use

1.OverlayWindow�͈ȉ��̃p�[�~�b�V�������K�v�ł��B
AndroidManifest�Ɉȉ����L�ڂ��Ă��������B  

	<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />

2.OverlayWindow �������� OverlayApplication���p�������N���X���쐬���Ă��������B  
  
3.onCreateView() ��override����(eclipse�Ȃ珟���Stub�ǉ������)  
�\����������View��ԋp���Ă��������B  

    protected View onCreateView(LayoutInflater inflater, ViewGroup root) {  
        View view = inflater.inflate(R.layout.overlay_video, root);
        return view;
    }

4.OverlayWindow ����� OverlayApplication��  
Service���p�������N���X�ł��B���p����(�p������)�N���X��
AndroidManifest��Service�Ƃ��āA�L�ڂ��Ă��������B  

	<service android:name=".HogeHogeOverlayWindowService"></service>

Activity����ȉ��̂悤�ɋN���ł��܂��B  

	startService(new Intent(getApplicationContext(), HogeHogeOverlayWindowService.class));


## ���C�Z���X
----------
Copyright &copy; 2013 Ny Project 
�M�҂�Eclipse�ł̊J�������\�z���Ă��邽�߁A
�ꕔ�AEclipse�݂̂ł������Ă͂܂�Ȃ��L�ڂ�����܂��B  

1. Android �A�v���P�[�V�����J�����̍\�z  
   �ȗ����܂��B
   
2. Android �A�v���P�[�V�����v���W�F�N�g�̍쐬(���Ȃ��̃A�v��)  
   �ȗ����܂��B
   
3. OverlayWindow��Library�Ƃ��ēo�^����(��������o���ł��B�B)  
   Eclipse �ŁA �C���|�[�g -> �����v���W�F�N�g�����[�N�X�y�[�X��  
   ��OK���Ǝv���܂��B  
   
4. �g�p�������A�v���P�[�V�����̃v���W�F�N�g��OverlayWindow Library��ǉ�  
   Eclipse �ŁA �A�v���P�[�V�����̃v���W�F�N�g���E�N���b�N  
   �v���p�f�B -> Android -> ���C�u���� �ŁAOverlayWindow ��ǉ�  


## How to use

1.OverlayWindow�͈ȉ��̃p�[�~�b�V�������K�v�ł��B
AndroidManifest�Ɉȉ����L�ڂ��Ă��������B  

	<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
2.OverlayWindow �������� OverlayApplication���p�������N���X���쐬���Ă��������B  
3.onCreateView() ��override����(eclipse�Ȃ珟���Stub�ǉ������)  
�\����������View��ԋp���Ă��������B  

    protected View onCreateView(LayoutInflater inflater, ViewGroup root) {  
        View view = inflater.inflate(R.layout.overlay_video, root);
        return view;
    }

4.OverlayWindow ����� OverlayApplication��
Service���p�������N���X�ł��B���p����(�p������)�N���X��
AndroidManifest��Service�Ƃ��āA�L�ڂ��Ă��������B  

	<service android:name=".HogeHogeOverlayWindowService"></service>

Activity����ȉ��̂悤�ɋN���ł��܂��B  

	startService(new Intent(getApplicationContext(), HogeHogeOverlayWindowService.class));


## ���C�Z���X
----------
Copyright &copy; 2013 Ny Project 