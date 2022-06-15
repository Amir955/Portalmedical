
import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { ToastrService } from 'ngx-toastr';
import { AccountService } from 'src/app/services/account.service';
import { UserEntity } from 'src/models/userEntity';
import { AjoutpatientComponent } from './ajoutpatient/ajoutpatient.component';
import { UpdatepatientComponent } from './updatepatient/updatepatient.component';
@Component({
  selector: 'app-gestion-des-patients',
  templateUrl: './gestion-des-patients.component.html',
  styleUrls: ['./gestion-des-patients.component.css']
}) 
export class GestionDesPatientsComponent implements  AfterViewInit , OnInit {
  displayedColumns: string[] = [ 'email', 'nom', 'prenom','sexe','age','creation_date','numtelephone','Actions'];
  private defaultImage = 'assets/img/logo.png';
    public imageUrl: string ;
  dataSource: MatTableDataSource<any>;
  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;
  users: UserEntity[];
  images: string[];
  base64Data: Int8Array;
  retrievedImage: string;
  lang: any;

  constructor( private accountService: AccountService ,
               private toast: ToastrService,
               private dialog: MatDialog) {
    this.dataSource = new MatTableDataSource(this.users);
  }
  ngOnInit(): void {
this.getAllUser()  }


  ngAfterViewInit() {
    this.getAllUser();
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;

  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }
  getAllUser(){
    this.accountService.getAllUsers().subscribe(data => {
        this.dataSource.data = data;
      }
    );
  }
  deleteUser(id: number){
    const confirm = window.confirm('do you want to delete this user');
    if (confirm) {
      this.accountService.deleteUser(id).subscribe(res => {
          this.toast.success('user deleted ', 'delete', {
            timeOut: 3000,
            positionClass: 'toast-bottom-right'});
          this.ngAfterViewInit();
        },
        error => this.toast.error('something wrong '));
    }}
  onEdit(row){
    const dialogConfig = new MatDialogConfig();
    dialogConfig.disableClose = true;
    dialogConfig.autoFocus = true;
    dialogConfig.width = '60%';
    dialogConfig.data = row;
    this.dialog.open(UpdatepatientComponent, dialogConfig);

    this.dialog.afterAllClosed.subscribe(() => this.ngAfterViewInit());
  }
 
  
 onError(): void {
    this.retrievedImage = this.defaultImage;
  }

  add(){

    const dialogConfig = new MatDialogConfig();
    dialogConfig.disableClose = true;
    dialogConfig.autoFocus = true;
    dialogConfig.width = '60%';
    this.dialog.open(AjoutpatientComponent, dialogConfig);

    this.dialog.afterAllClosed.subscribe(() => this.ngAfterViewInit());
  }
}
